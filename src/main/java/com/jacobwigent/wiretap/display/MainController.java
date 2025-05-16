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


public class MainController implements SerialListener {

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

    /*
        Populates fields with available ports and baud rates.
        Called by the FXML loader after the FXML file is loaded and all @FXML-annotated elements have been injected.
     */
    @FXML
    public void initialize() {
        messageHandler = new MessageHandler(serialMonitor, serialPlotter);
        loadAvailablePorts();
        loadBaudRates();
        SerialService.addListener(this);
    }

    @FXML
    private void loadAvailablePorts() {
        String[] newPorts = SerialService.getAvailablePortNames();
        // Only reload if newPorts have changed
        if (comparePortList(previousPorts, newPorts)) { return; }
        previousPorts = new ArrayList<>(Arrays.asList(newPorts));
        portComboBox.getItems().clear();
        portComboBox.getItems().addAll(newPorts);
        updateConnectionInfo();
        updateSerialStats();
    }

    private boolean comparePortList(ArrayList<String> list, String[] array) {
        if (list == null) { return false; }
        if (list.size() != array.length) {
            return false;
        }
        for (int i = 0; i < list.size(); i++) {
            if (!list.get(i).equals(array[i])) {
                return false;
            }
        }
        return true;
    }

    protected void loadBaudRates() {
        ArrayList<Integer> newRates = SerialService.getBaudRates();
        if (newRates.equals(previousBauds)) { return; }
        baudComboBox.getItems().clear();
        for (int baudRate : newRates) {
            baudComboBox.getItems().add(Integer.toString(baudRate));
        }
        previousBauds = newRates;
        updateConnectionInfo();
        updateSerialStats();
        onConnectionOptionsChange();
    }

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

        // Test for connection to prevent softlocking if all baud options are removed
        if(!connected) {
            connectButton.setDisable(!validOptions);
        }
    }

    @FXML
    protected void onConnectClick() {
        // Update UI immediately
        connectButton.setDisable(true);
        portComboBox.setDisable(true);
        baudComboBox.setDisable(true);
        connectionUpdateLabel.setText(connected ? "Disconnecting..." : "Connecting...");

        // Run connection logic in a background thread
        new Thread(() -> {
            boolean successful;

            if (connected) {
                successful = SerialService.tryDisconnect();
                connected = !successful;
            } else {
                successful = SerialService.tryConnect();
                connected = successful;
                if (successful) {
                    messageHandler.flush();
                }
            }

            // Update UI back on the JavaFX thread
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

    private void updateConnectionInfo() {
        connectButton.setText(connected ? "Disconnect" : "Connect");
        portComboBox.setDisable(connected);
        baudComboBox.setDisable(connected);

        // Disables button if baud rate is removed, ONLY after any connection is ended
        if (baudComboBox.getValue() == null && !connected) {
            connectButton.setDisable(true);
        }

        connectionStatusLabel.setText(connected ? "Connected to " + SerialService.getConnectionInfo() : "Not Connected");
    }

    @Override
    public void onSerialData(SerialMessage data) {
        javafx.application.Platform.runLater(this::updateSerialStats);
    }

    @Override
    public void onDisconnect() {
        javafx.application.Platform.runLater(() -> {
            SerialService.tryDisconnect();
            connected = false;
            updateConnectionInfo();
            connectionUpdateLabel.setText("Lost Connection");
        });
    }

    @FXML
    public void clearMonitor() {
        serialMonitor.clear();
        serialPlotter.clear();
    }

    private void updateSerialStats() {
        String text =
                "Message Count: " + messageHandler.getMessageCount() + "\n" +
                "Line Count: " + messageHandler.getLineCount() + "\n" +
                "Message Rate: " + messageHandler.getAverageRate() + "ms\n" +
                "Connection Time: " + Utilities.formatTime(SerialService.getElapsedConnectionTime()) + "\n";
        serialStatistics.setText(text);
    }

    @FXML
    public void updateFreeze() {
        messageHandler.setFreeze(freezeToggle.isSelected());
    }

    @FXML
    public void updateScroll() {
        serialMonitor.setAutoScroll(scrollToggle.isSelected());
    }

    @FXML
    public void closeApplication() {
        javafx.application.Platform.exit();
    }

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

    @FXML
    public void openSourceCode() {
        try {
            Desktop.getDesktop().browse(new URI("https://github.com/jacob-wigent/wire-tap"));
        } catch (Exception ignored) {}
    }

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

    public String getSelectedPort() {
        return selectedPort;
    }

    public void setSelectedPort(String selectedPort) {
        this.selectedPort = selectedPort;
        portComboBox.setValue(selectedPort);
        onConnectionOptionsChange();
    }
}
