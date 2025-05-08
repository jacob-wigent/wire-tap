package com.jacobwigent.wiretap.display;

import com.jacobwigent.wiretap.WireTap;
import com.jacobwigent.wiretap.serial.SerialListener;
import javafx.event.ActionEvent;
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
import javafx.stage.StageStyle;

import java.awt.*;
import java.io.IOException;
import java.net.URI;


public class MainController implements SerialListener {

    @FXML private Label serialStatistics;
    @FXML private ComboBox<String> portComboBox;
    @FXML private ComboBox<String> baudComboBox;
    @FXML private SerialMonitor serialMonitor;
    @FXML private Button connectButton;
    @FXML private Label connectionUpdateLabel;
    @FXML private Label connectionStatusLabel;
    @FXML private CheckBox freezeToggle;

    private boolean connected = false;
    private String selectedPort = null;
    private String selectedBaudRate = null;

    /*
        Populates fields with available ports and baud rates.
        Called by the FXML loader after the FXML file is loaded and all @FXML-annotated elements have been injected.
     */
    @FXML
    public void initialize() {
        loadAvailablePorts();
        loadBaudRates();
        SerialService.setListener(this);
    }

    @FXML
    private void loadAvailablePorts() {
        // TODO: Only reload if ports have changed
        portComboBox.getItems().clear();
        portComboBox.getItems().addAll(SerialService.getAvailablePortNames());
        updateConnectionInfo();
        updateSerialStats();
    }

    protected void loadBaudRates() {
        baudComboBox.getItems().clear();
        for (int baudRate : SerialService.baudRates) {
            baudComboBox.getItems().add(Integer.toString(baudRate));
        }
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
            }

            // Update UI back on the JavaFX thread
            javafx.application.Platform.runLater(() -> {
                connectButton.setDisable(false);
                connectionUpdateLabel.setText(successful
                        ? (connected ? "Connected Successfully" : "Disconnected Successfully")
                        : (connected ? "Failed to Disconnect" : "Failed to Connect"));
                updateConnectionInfo();
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

        connectionStatusLabel.setText(connected ? "Connected to " + SerialService.getCurrentPort().getDescriptivePortName() + " @ " + SerialService.getCurrentPort().getBaudRate() : "Not Connected");
    }

    @Override
    public void onSerialData(String data) {
        javafx.application.Platform.runLater(() -> {
                    serialMonitor.print(data);
                    updateSerialStats();
                }
        );
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
    }

    private void updateSerialStats() {
        String text =
                "Message Count: " + SerialService.getMessageCount() + "\n" +
                "Connection Time: " + SerialMessage.formatTime(SerialService.getElapsedConnectionTime()) + "\n";
        serialStatistics.setText(text);
    }

    @FXML
    public void updateFreeze() {
        serialMonitor.setFreeze(freezeToggle.isSelected());
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
