package com.jacobwigent.wiretap.display;

import com.jacobwigent.wiretap.serial.SerialListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import com.jacobwigent.wiretap.serial.SerialService;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import javax.usb.UsbException;
import javax.usb.UsbHostManager;
import javax.usb.UsbServices;
import javax.usb.event.UsbServicesEvent;
import javax.usb.event.UsbServicesListener;


public class Controller implements SerialListener, UsbServicesListener {

    @FXML private Label serialStatistics;
    @FXML private ComboBox<String> portComboBox;
    @FXML private ComboBox<String> baudComboBox;
    @FXML private SerialMonitor serialMonitor;
    @FXML private Button connectButton;
    @FXML private Label connectionUpdateLabel;
    @FXML private Label connectionStatusLabel;
    @FXML private CheckBox freezeToggle;

    private boolean connected = false;

    /*
        Populates fields with available ports and baud rates.
        Called by the FXML loader after the FXML file is loaded and all @FXML-annotated elements have been injected.
     */
    @FXML
    public void initialize() {
        loadAvailablePorts();
        SerialService.setListener(this);
        UsbServices services = null;
        try {
            services = UsbHostManager.getUsbServices();
        } catch (UsbException e) {
            throw new RuntimeException(e);
        }
        services.addUsbServicesListener(this);
    }

    private void loadAvailablePorts() {
        portComboBox.getItems().clear();
        for (int baudRate : SerialService.baudRates) {
            baudComboBox.getItems().add(Integer.toString(baudRate));
        }
        portComboBox.getItems().addAll(SerialService.getAvailablePorts());
        updateConnectionInfo();
        updateSerialStats();
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

    @Override
    public void usbDeviceAttached(UsbServicesEvent usbServicesEvent) {
        javafx.application.Platform.runLater(this::loadAvailablePorts);
    }

    @Override
    public void usbDeviceDetached(UsbServicesEvent usbServicesEvent) {
        javafx.application.Platform.runLater(this::loadAvailablePorts);
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
}
