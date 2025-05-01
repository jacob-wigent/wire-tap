package com.jacobwigent.wiretap;

import com.jacobwigent.wiretap.serial.SerialListener;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import com.jacobwigent.wiretap.serial.SerialService;

public class Controller implements SerialListener {

    @FXML private Label serialStatistics;
    @FXML private ComboBox<String> portComboBox;
    @FXML private ComboBox<String> baudComboBox;
    @FXML private SerialMonitor serialMonitor;
    @FXML private Button connectButton;
    @FXML private Label connectionUpdateLabel;
    @FXML private Label connectionStatusLabel;

    private boolean connected = false;
    private long messagesCount;

    /*
        Populates fields with available ports and baud rates.
        Called by the FXML loader after the FXML file is loaded and all @FXML-annotated elements have been injected.
     */
    @FXML
    public void initialize() {
        for (int baudRate : SerialService.baudRates) {
            baudComboBox.getItems().add(Integer.toString(baudRate));
        }
        portComboBox.getItems().addAll(SerialService.getAvailablePorts());
        updateConnectionInfo();
        updateSerialStats();
        SerialService.setListener(this);
    }

    @FXML
    protected void onConnectionOptionsChange() {
        String port = portComboBox.getValue();
        String baud = baudComboBox.getValue();

        boolean validOptions = port != null && baud != null;

        if (validOptions) {
            SerialService.selectPort(port);
            SerialService.selectBaudRate(Integer.parseInt(baud));
        }

        connectButton.setDisable(!validOptions);
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

        connectionStatusLabel.setText(connected ? "Connected to " + SerialService.getCurrentPort().getDescriptivePortName() + " @ " + SerialService.getCurrentPort().getBaudRate() : "Not Connected");
    }

    @Override
    public void onSerialData(String data) {
        javafx.application.Platform.runLater(() -> {
                    serialMonitor.print(data);
                    messagesCount = SerialService.getMessageCount();
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
        String text = "Message Count: " + messagesCount + "\n";
        serialStatistics.setText(text);
    }
}
