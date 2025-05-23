package com.jacobwigent.wiretap.display;

import com.jacobwigent.wiretap.serial.SerialLine;
import com.jacobwigent.wiretap.serial.SerialMessage;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseButton;

public class SerialMonitor extends ListView<SerialLine> {

    private boolean autoScroll = true;

    private final ObservableList<SerialLine> displayedLines = FXCollections.observableArrayList();

    public SerialMonitor() {
        this.setItems(displayedLines);

        this.setCellFactory(list -> new ListCell<>() {
            private ListChangeListener<SerialMessage> listener;
            {
                setOnMouseClicked(event -> {
                    if (event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() == 2) {
                        SerialLine line = getItem();
                        if (line != null) {
                            System.out.println("Double clicked on: " + line.getLineText());
                        }
                    }
                });
            }
            @Override
            protected void updateItem(SerialLine line, boolean empty) {
                super.updateItem(line, empty);

                // Remove old listener from previous item
                if (listener != null && getItem() != null) {
                    getItem().getMessages().removeListener(listener);
                    listener = null;
                }

                if (empty || line == null) {
                    setText(null);
                } else {
                    // Set initial text
                    setText(line.getLineText());

                    // Register listener to update text on future changes
                    listener = change -> javafx.application.Platform.runLater(() -> setText(line.getLineText()));
                    line.getMessages().addListener(listener);
                }
            }
        });
        displayedLines.addListener((ListChangeListener<SerialLine>) change -> {
            while (change.next()) {
                if (autoScroll && change.wasAdded()) {
                    int newIndex = change.getFrom();
                    javafx.application.Platform.runLater(() -> scrollTo(newIndex));
                }
            }
        });
    }

    public void addLine(SerialLine line) {
        javafx.application.Platform.runLater(() -> {
            displayedLines.add(line);
        });
    }

    public void clear() {
        javafx.application.Platform.runLater(() -> {
            displayedLines.clear();
            this.refresh();
        });
    }


    public void setAutoScroll(boolean autoScroll) {
        this.autoScroll = autoScroll;
    }
}
