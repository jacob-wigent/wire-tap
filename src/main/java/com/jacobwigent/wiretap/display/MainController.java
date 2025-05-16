package com.jacobwigent.wiretap.display;

import com.jacobwigent.wiretap.WireTap;
import com.jacobwigent.wiretap.serial.MessageHandler;
import com.jacobwigent.wiretap.serial.SerialListener;
import com.jacobwigent.wiretap.serial.SerialMessage;
import com.jacobwigent.wiretap.util.Utilities;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import com.jacobwigent.wiretap.serial.SerialService;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;


/**
 * MainController handles the logic for the main GUI window.
 * It manages serial port connections, user input for port and baud rate,
 * and updates the serial monitor and plotter displays based on incoming data.
 */
public class MainController implements SerialListener {

    // FXML-injected fields for UI controls
    @FXML private Label serialStatistics;
    @FXML private ComboBox<String> portComboBox;
    @FXML private ComboBox<String> baudComboBox;
    @FXML private SerialMonitor serialMonitor;
    @FXML private SerialPlotter serialPlotter;
    @FXML private Button connectButton;
    @FXML private Label connectionUpdateLabel;
    @FXML private Label connectionStatusLabel;
    @FXML private CheckBox freezeToggle;
    @FXML private CheckBox scrollToggle;

    private MessageHandler messageHandler;
    private boolean connected = false;
    private String selectedPort = null;
    private String selectedBaudRate = null;
    private ArrayList<String> previousPorts;
    private ArrayList<Integer> previousBauds;

    /**
     * Called automatically after FXML is loaded.
     * Initializes the message handler, populates available ports and baud rates,
     * and registers this controller as a SerialListener.
     */
    @FXML
    public void initialize() {
        messageHandler = new MessageHandler(serialMonitor, serialPlotter);
        loadAvailablePorts();
        loadBaudRates();
        SerialService.addListener(this);
    }

    /**
     * Loads the list of currently available serial ports into the combo box.
     * Skips update if the port list hasn't changed.
     */
    @FXML
    private void loadAvailablePorts() {
        String[] newPorts = SerialService.getAvailablePortNames();
        if (comparePortList(previousPorts, newPorts)) { return; }

        previousPorts = new ArrayList<>(Arrays.asList(newPorts));
        portComboBox.getItems().clear();
        portComboBox.getItems().addAll(newPorts);

        updateConnectionInfo();
        updateSerialStats();
    }

    /**
     * Compares a previous list of ports to a new array to determine if the UI needs updating.
     *
     * @param list  Previously stored list of port names
     * @param array New list of port names
     * @return true if lists are identical, false otherwise
     */
    private boolean comparePortList(ArrayList<String> list, String[] array) {
        if (list == null) return false;
        if (list.size() != array.length) return false;

        for (int i = 0; i < list.size(); i++) {
            if (!list.get(i).equals(array[i])) {
                return false;
            }
        }
        return true;
    }

    /**
     * Loads available baud rates into the combo box.
     * Prevents reloading if baud rates haven't changed.
     */
    protected void loadBaudRates() {
        ArrayList<Integer> newRates = SerialService.getBaudRates();
        if (newRates.equals(previousBauds)) return;

        baudComboBox.getItems().clear();
        for (int baudRate : newRates) {
            baudComboBox.getItems().add(Integer.toString(baudRate));
        }

        previousBauds = newRates;

        updateConnectionInfo();
        updateSerialStats();
        onConnectionOptionsChange();
    }

    /**
     * Updates internal state and SerialService when the user changes connection settings.
     * Enables the connect button only when valid options are selected.
     */
    @FXML
    protected void onConnectionOptionsChange() {
        String port = portComboBox.getValue();
        String baud = baudComboBox.getValue();

        selectedPort = port;
        selectedBaudRate = baud;

        boolean validOptions = port != null && baud != null;

        if (validOptions) {
            SerialService.selectPort(port);
            SerialService.selectBaudRate(Integer.parseInt(baud));
        }

        // Prevent user from clicking connect if the selection is incomplete
        if (!connected) {
            connectButton.setDisable(!validOptions);
        }
    }

    /**
     * Handles connect/disconnect button clicks.
     * Performs connection in a background thread to avoid blocking the UI.
     */
    @FXML
    protected void onConnectClick() {
        // Disable controls immediately for responsiveness
        connectButton.setDisable(true);
        portComboBox.setDisable(true);
        baudComboBox.setDisable(true);
        connectionUpdateLabel.setText(connected ? "Disconnecting..." : "Connecting...");

        // Create background thread to avoid locking UI while conencting
        new Thread(() -> {
            boolean successful;

            if (connected) {
                // Try disconnecting
                successful = SerialService.tryDisconnect();
                connected = !successful;
            } else {
                // Try connecting
                successful = SerialService.tryConnect();
                connected = successful;
                if (successful) {
                    messageHandler.flush(); // Clear any previous serial data
                }
            }

            // Update UI elements on JavaFX Application thread
            javafx.application.Platform.runLater(() -> {
                connectButton.setDisable(false);
                connectionUpdateLabel.setText(successful
                        ? (connected ? "Connected Successfully" : "Disconnected Successfully")
                        : (connected ? "Failed to Disconnect" : "Failed to Connect"));
                updateConnectionInfo();
                updateSerialStats();
            });
        }).start();
    }

    /**
     * Updates the connection UI elements based on current connection state.
     * Called whenever connection or combo box values change.
     */
    private void updateConnectionInfo() {
        connectButton.setText(connected ? "Disconnect" : "Connect");
        portComboBox.setDisable(connected);
        baudComboBox.setDisable(connected);

        // Prevents user from connecting with no baud rate selected
        if (baudComboBox.getValue() == null && !connected) {
            connectButton.setDisable(true);
        }

        connectionStatusLabel.setText(connected
                ? "Connected to " + SerialService.getConnectionInfo()
                : "Not Connected");
    }

    @Override
    public void onSerialData(SerialMessage data) {
        // Update stats on the JavaFX thread when new serial data arrives
        javafx.application.Platform.runLater(this::updateSerialStats);
    }

    /**
     * Handles loss of serial connection by forcing a disconnect update and updating UI.
     */
    @Override
    public void onDisconnect() {
        // Clean up UI and state when a disconnect is detected
        javafx.application.Platform.runLater(() -> {
            SerialService.tryDisconnect();
            connected = false;
            updateConnectionInfo();
            connectionUpdateLabel.setText("Lost Connection");
        });
    }

    /**
     * Clears both the text-based serial monitor and graphical plotter display.
     */
    @FXML
    public void clearMonitor() {
        serialMonitor.clear();
        serialPlotter.clear();
    }

    /**
     * Updates the label showing serial statistics such as message rate, count, and connection time.
     */
    private void updateSerialStats() {
        String text =
                "Message Count: " + messageHandler.getMessageCount() + "\n" +
                        "Line Count: " + messageHandler.getLineCount() + "\n" +
                        "Message Rate: " + messageHandler.getAverageRate() + "ms\n" +
                        "Connection Time: " + Utilities.formatTime(SerialService.getElapsedConnectionTime()) + "\n";
        serialStatistics.setText(text);
    }

    /**
     * Toggles whether the serial input is being diplayed
     */
    @FXML
    public void updateFreeze() {
        messageHandler.setFreeze(freezeToggle.isSelected());
    }

    /**
     * Enables or disables automatic scrolling in the monitor display.
     */
    @FXML
    public void updateScroll() {
        serialMonitor.setAutoScroll(scrollToggle.isSelected());
    }

    /**
     * Cleanly quits the application via JavaFX exit function.
     */
    @FXML
    public void closeApplication() {
        javafx.application.Platform.exit();
    }

    /**
     * Opens a modal dialog allowing the user to edit the list of available baud rates.
     */
    @FXML
    public void openBaudRateMenu() {
        FXMLLoader loader = new FXMLLoader(WireTap.class.getResource("baud-config-view.fxml"));

        Parent root;
        try {
            root = loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        BaudConfigController controller = loader.getController();
        controller.setMainController(this);

        Stage dialog = new Stage();
        dialog.setTitle("Baud Rate Configuration");
        dialog.setScene(new Scene(root));
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setResizable(false);
        dialog.showAndWait();
    }

    /**
     * Opens a simple "About" modal showing app version, author, and licensing info.
     */
    @FXML
    public void openAboutMenu() {
        Label infoLabel = new Label("WireTap v" + WireTap.VERSION +
                "\n\nSerial monitor for microcontroller debugging.\n\n" +
                "Author: Jacob Wigent\n" +
                "MIT License\n\n" +
                "GitHub: github.com/jwigent/wiretap");

        infoLabel.setAlignment(Pos.CENTER);

        VBox root = new VBox(infoLabel);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(20));

        Scene scene = new Scene(root);
        Stage dialog = new Stage();
        dialog.setTitle("About");
        dialog.setScene(scene);
        dialog.setResizable(false);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.showAndWait();
    }

    /**
     * Opens the GitHub project page for WireTap in the system browser.
     */
    @FXML
    public void openSourceCode() {
        try {
            Desktop.getDesktop().browse(new URI("https://github.com/jacob-wigent/wire-tap"));
        } catch (Exception ignored) {}
    }

    /**
     * Opens a modal window showing detailed information about each available serial port.
     */
    @FXML
    public void openPortInspector() {
        FXMLLoader loader = new FXMLLoader(WireTap.class.getResource("port-inspector-view.fxml"));

        Parent root;
        try {
            root = loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        PortInspectorController controller = loader.getController();
        controller.setMainController(this);

        Stage dialog = new Stage();
        dialog.setTitle("Port Inspector");
        dialog.setScene(new Scene(root));
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.showAndWait();
    }

    /**
     * Returns the currently selected serial port name.
     * @return the selected system port name as a String
     */
    public String getSelectedPort() {
        return selectedPort;
    }

    /**
     * Sets the selected port and updates dependent UI and connection settings.
     * @param selectedPort the name of the port to select
     */
    public void setSelectedPort(String selectedPort) {
        this.selectedPort = selectedPort;
        portComboBox.setValue(selectedPort);
        onConnectionOptionsChange();
    }
}
