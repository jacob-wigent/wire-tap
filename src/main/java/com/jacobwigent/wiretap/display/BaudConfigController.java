package com.jacobwigent.wiretap.display;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import com.jacobwigent.wiretap.serial.SerialService;

import java.util.ArrayList;

/**
 * Controller for the baud rate configuration window.
 * Allows users to add, remove, and save a custom list of baud rates.
 */
public class BaudConfigController {

    @FXML private VBox baudRateList;
    @FXML private TextField baudRateInput;
    @FXML private Button saveButton;

    private ArrayList<Integer> baudRates;
    private MainController mainController;

    /**
     * Initializes the controller after its root element has been completely processed.
     * Loads current baud rates from SerialService and sets up UI.
     */
    @FXML
    public void initialize() {
        baudRates = new ArrayList<>(SerialService.getBaudRates());
        saveButton.setDisable(true);
        baudRateList.setPadding(new Insets(6, 6, 6, 6));
        updateDisplay();
    }

    /**
     * Provides a reference to the main controller so it can be updated when this window is closed.
     *
     * @param mainController the main controller instance
     */
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    /**
     * Refreshes the VBox list displaying the current set of baud rates.
     * Each baud rate is shown with a label and a remove button.
     */
    private void updateDisplay() {
        baudRateList.getChildren().clear();

        for (Integer rate : baudRates) {
            HBox row = new HBox(6);
            Label label = new Label(rate.toString());
            Button remove = new Button("✕");

            // Removes this baud rate from the list when clicked
            remove.setOnAction(e -> {
                baudRates.remove(rate);
                updateDisplay();
                saveButton.setDisable(false);
            });

            // Spacer pushes the remove button to the right
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            // Style the remove button as a subtle X icon
            remove.setStyle(
                    "-fx-background-color: transparent;" +
                            "-fx-font-size: 14px;" +
                            "-fx-padding: 2 6 2 6;" +
                            "-fx-cursor: hand;"
            );

            row.setAlignment(Pos.CENTER_LEFT);
            row.getChildren().addAll(label, spacer, remove);
            baudRateList.getChildren().add(row);
        }
    }

    /**
     * Called when the user clicks the add button.
     * Attempts to parse the input as an integer and add it to the list if not already present.
     */
    @FXML
    public void onAddBaudRate() {
        try {
            int rate = Integer.parseInt(baudRateInput.getText().trim());
            if (!baudRates.contains(rate)) {
                baudRates.add(rate);
                updateDisplay();
                baudRateInput.clear();
            }
        } catch (NumberFormatException ignored) {
            // Silently ignore invalid input
        }
        saveButton.setDisable(false);
    }

    /**
     * Closes the baud rate configuration window and reloads baud rates in the main UI.
     */
    @FXML
    public void onClose() {
        Stage stage = (Stage) baudRateList.getScene().getWindow();
        stage.close();
        mainController.loadBaudRates();
    }

    /**
     * Saves the current baud rate list to the SerialService and disables the save button.
     */
    @FXML
    public void onSave() {
        SerialService.setBaudRates(baudRates);
        saveButton.setDisable(true);
        mainController.loadBaudRates();
    }
}
