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

public class BaudConfigController {

    @FXML private VBox baudRateList;
    @FXML private TextField baudRateInput;
    @FXML private Button saveButton;

    private ArrayList<Integer> baudRates;
    private MainController mainController;

    @FXML
    public void initialize() {
        baudRates = new ArrayList<>(SerialService.getBaudRates());
        saveButton.setDisable(true);
        baudRateList.setPadding(new Insets(6, 6, 6, 6));
        updateDisplay();
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    private void updateDisplay() {
        baudRateList.getChildren().clear();

        for (Integer rate : baudRates) {
            HBox row = new HBox(6);
            Label label = new Label(rate.toString());
            Button remove = new Button("✕");

            remove.setOnAction(e -> {
                baudRates.remove(rate);
                updateDisplay();
                saveButton.setDisable(false);
            });

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
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

    @FXML
    public void onAddBaudRate() {
        try {
            int rate = Integer.parseInt(baudRateInput.getText().trim());
            if (!baudRates.contains(rate)) {
                baudRates.add(rate);
                updateDisplay();
                baudRateInput.clear();
            }
        } catch (NumberFormatException ignored) {}
        saveButton.setDisable(false);
    }

    @FXML
    public void onClose() {
        Stage stage = (Stage) baudRateList.getScene().getWindow();
        stage.close();
        mainController.loadBaudRates();
    }

    @FXML
    public void onSave() {
        SerialService.setBaudRates(baudRates);
        saveButton.setDisable(true);
        mainController.loadBaudRates();
    }
}
