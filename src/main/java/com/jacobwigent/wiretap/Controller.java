package com.jacobwigent.wiretap;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import com.jacobwigent.wiretap.serial.SerialService;

public class Controller {

    @FXML private ComboBox<String> portComboBox;
    @FXML private ComboBox<String> baudComboBox;
    @FXML private TextArea serialOutput;
    @FXML private Button connectButton;

    /*
        Populates fields with available ports and baud rates.
        Called by the FXML loader after the FXML file is loaded and all @FXML-annotated elements have been injected.
     */
    @FXML
    public void initialize() {
        for (int baudRate : SerialService.baudRates) {
            baudComboBox.getItems().add(Integer.toString(baudRate));
        }
        portComboBox.getItems().addAll(SerialService.getAvaiablePorts());
    }

    @FXML
    protected void validateConnectionOptions() {
        boolean validOptions = portComboBox.getValue() != null && baudComboBox.getValue() != null;
        connectButton.setDisable(!validOptions);
    }

    @FXML
    protected void onConnectClick() {
        String port = portComboBox.getValue();
        String baud = baudComboBox.getValue();
        serialOutput.appendText("Connecting to " + port + " @ " + baud + " baud\n");
    }

    @FXML
    protected void onSaveClick() {
        serialOutput.appendText("Saving to file...\n");
    }
}
