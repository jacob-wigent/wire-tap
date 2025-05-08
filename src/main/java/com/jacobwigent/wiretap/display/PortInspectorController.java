package com.jacobwigent.wiretap.display;

import com.fazecast.jSerialComm.SerialPort;
import com.jacobwigent.wiretap.serial.SerialService;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.util.*;

public class PortInspectorController {

    @FXML VBox portList;
    @FXML private Button selectButton;

    private List<SerialPort> serialPorts;
    private MainController mainController;
    private String selectedPort = null;

    @FXML
    public void initialize() {
        serialPorts = Arrays.asList(SerialService.getAvailablePorts());
        selectButton.setDisable(true);
        portList.setPadding(new Insets(6, 6, 6, 6));
        updateDisplay();
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
        this.selectedPort = mainController.getSelectedPort();
        updateDisplay();
    }

    private void updateDisplay() {
        portList.getChildren().clear();

        for (SerialPort port : serialPorts) {
            HBox row = new HBox(10);
            row.setPadding(new Insets(6));
            row.setAlignment(Pos.CENTER_LEFT);

            // Main COM port (bold and large)
            Label comLabel = new Label(port.getSystemPortName());
            comLabel.setFont(Font.font("System", FontWeight.BOLD, 18));

            // Descriptive name
            String description = port.getDescriptivePortName();
            Label descLabel = new Label(Optional.ofNullable(description).orElse("Unknown"));
            descLabel.setFont(Font.font(13));

            // Optional extra info
            Label manufacturer = new Label("Manufacturer: " + Optional.ofNullable(port.getManufacturer()).orElse("Unknown"));
            Label portDescription = new Label("Description: " + Optional.ofNullable(port.getPortDescription()).orElse("Unknown"));
            Label serial = new Label("Serial #: " + Optional.ofNullable(port.getSerialNumber()).orElse("Unknown"));
            manufacturer.setTextFill(Color.DARKGRAY);
            portDescription.setTextFill(Color.DARKGRAY);
            serial.setTextFill(Color.DARKGRAY);

            VBox infoBox = new VBox(2, comLabel, descLabel, manufacturer, portDescription, serial);

            // Highlight selected port
            if (selectedPort != null && selectedPort.equals(port.getSystemPortName())) {
                row.setStyle("-fx-background-color: rgba(0, 0, 0, 0.1);");
            }

            row.getChildren().add(infoBox);

            row.setOnMouseClicked(e -> {
                if (!Objects.equals(selectedPort, port.getSystemPortName())) {
                    selectButton.setDisable(false);
                }
                selectedPort = port.getSystemPortName();
                updateDisplay();
            });

            portList.getChildren().add(row);
        }


        if (SerialService.isConnected()) {
            selectButton.setDisable(true);
        }
    }

    @FXML
    public void onClose() {
        Stage stage = (Stage) portList.getScene().getWindow();
        stage.close();
    }

    @FXML
    public void onSelect() {
        if(SerialService.isConnected()) { return; }
        selectButton.setDisable(true);
        mainController.setSelectedPort(selectedPort);
    }

    @FXML
    private void reloadPorts() {
        serialPorts = Arrays.asList(SerialService.getAvailablePorts());
        boolean selectedPortRemoved = true;
        for (SerialPort port : serialPorts) {
            if (port.getSystemPortName().equals(selectedPort)) {
                selectedPortRemoved = false;
            }
        }
        if (selectedPortRemoved) {
            selectedPort = null;
        }
        updateDisplay();
    }

}
