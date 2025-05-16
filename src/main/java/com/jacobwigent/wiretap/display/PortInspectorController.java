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

/**
 * PortInspectorController handles the logic for the port inspector modal.
 */
public class PortInspectorController {

    @FXML VBox portList;
    @FXML private Button selectButton;

    private List<SerialPort> serialPorts;
    private MainController mainController;
    private String selectedPort = null;

    /**
     * Initializes the port inspector view and loads available serial ports.
     */
    @FXML
    public void initialize() {
        serialPorts = Arrays.asList(SerialService.getAvailablePorts());
        selectButton.setDisable(true);
        portList.setPadding(new Insets(6, 6, 6, 6));
        updateDisplay();
    }

    /**
     * Injects the MainController instance and updates the display with the currently selected port.
     *
     * @param mainController the application's main controller
     */
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
        this.selectedPort = mainController.getSelectedPort();
        updateDisplay();
    }

    /**
     * Updates the UI list of serial ports, highlighting the selected one.
     * Rebuilds the view from scratch to reflect any changes.
     */
    private void updateDisplay() {
        portList.getChildren().clear();

        for (SerialPort port : serialPorts) {
            HBox row = new HBox(10);
            row.setPadding(new Insets(6));
            row.setAlignment(Pos.CENTER_LEFT);

            // COM port name (bold and larger)
            Label comLabel = new Label(port.getSystemPortName());
            comLabel.setFont(Font.font("System", FontWeight.BOLD, 18));

            // Description text (usually includes USB info)
            String description = port.getDescriptivePortName();
            Label descLabel = new Label(Optional.ofNullable(description).orElse("Unknown"));
            descLabel.setFont(Font.font(13));

            // Optional detailed info
            Label manufacturer = new Label("Manufacturer: " + Optional.ofNullable(port.getManufacturer()).orElse("Unknown"));
            Label portDescription = new Label("Description: " + Optional.ofNullable(port.getPortDescription()).orElse("Unknown"));
            Label serial = new Label("Serial #: " + Optional.ofNullable(port.getSerialNumber()).orElse("Unknown"));

            // Gray out minor info for UI clarity
            manufacturer.setTextFill(Color.DARKGRAY);
            portDescription.setTextFill(Color.DARKGRAY);
            serial.setTextFill(Color.DARKGRAY);

            VBox infoBox = new VBox(2, comLabel, descLabel, manufacturer, portDescription, serial);

            // Highlight selected port
            if (selectedPort != null && selectedPort.equals(port.getSystemPortName())) {
                row.setStyle("-fx-background-color: rgba(0, 0, 0, 0.1);");
            }

            row.getChildren().add(infoBox);

            // Clicking the row will select that port and refresh the display
            row.setOnMouseClicked(e -> {
                if (!Objects.equals(selectedPort, port.getSystemPortName())) {
                    selectButton.setDisable(false);
                }
                selectedPort = port.getSystemPortName();
                updateDisplay();
            });

            portList.getChildren().add(row);
        }

        addEmulatedPort();

        if (SerialService.isConnected()) {
            selectButton.setDisable(true);
        }
    }

    /**
     * Adds the emulated serial port entry to the display.
     */
    private void addEmulatedPort() {
        HBox row = new HBox(10);
        row.setPadding(new Insets(6));
        row.setAlignment(Pos.CENTER_LEFT);

        Label comLabel = new Label("Emulated");
        comLabel.setFont(Font.font("System", FontWeight.BOLD, 18));

        String description = "Emulated Serial Port";
        Label descLabel = new Label(description);
        descLabel.setFont(Font.font(13));

        Label manufacturer = new Label("Manufacturer: Jacob Wigent");
        Label portDescription = new Label("Description: Emulated Serial Port for Testing");
        Label serial = new Label("Serial #: 123-456-789");

        manufacturer.setTextFill(Color.DARKGRAY);
        portDescription.setTextFill(Color.DARKGRAY);
        serial.setTextFill(Color.DARKGRAY);

        VBox infoBox = new VBox(2, comLabel, descLabel, manufacturer, portDescription, serial);

        if (selectedPort != null && selectedPort.equals("Emulated")) {
            row.setStyle("-fx-background-color: rgba(0, 0, 0, 0.1);");
        }

        row.getChildren().add(infoBox);

        row.setOnMouseClicked(e -> {
            if (!Objects.equals(selectedPort, "Emulated")) {
                selectButton.setDisable(false);
            }
            selectedPort = "Emulated";
            updateDisplay();
        });

        portList.getChildren().add(row);
    }

    /**
     * Closes the Port Inspector window.
     */
    @FXML
    public void onClose() {
        Stage stage = (Stage) portList.getScene().getWindow();
        stage.close();
    }

    /**
     * Called when the user selects a port and confirms.
     * Passes the selected port to the main controller.
     */
    @FXML
    public void onSelect() {
        if (SerialService.isConnected()) {
            return;
        }
        selectButton.setDisable(true);
        mainController.setSelectedPort(selectedPort);
    }

    /**
     * Reloads the list of available ports and updates the UI.
     * If the previously selected port was removed, it clears the selection.
     */
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

