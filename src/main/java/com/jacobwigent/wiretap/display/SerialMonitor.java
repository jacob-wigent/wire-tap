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
        this.setCellFactory(list -> new ListCell<SerialLine>() {
            {
                setOnMouseClicked(event -> {
                    if (event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() == 2) {
                        SerialLine line = getItem();
                        if (line != null) {
                            System.out.println("Double clicked on: " + line.toString());
                        }
                    }
                });
            }
            private ListChangeListener<SerialMessage> listener;

            @Override
            protected void updateItem(SerialLine line, boolean empty) {
                super.updateItem(line, empty);

                // Remove old listener
                if (listener != null && getItem() != null) {
                    getItem().getMessages().removeListener(listener);
                    listener = null;
                }

                if (empty || line == null) {
                    setText(null);
                    System.out.println("NULL SHIT");
                } else {
                    // show current content
                    javafx.application.Platform.runLater(() -> {
                        setText(line.toString());
                    });

                    // register a new listener so future add() calls re-render this cell
                    listener = change -> {
                        javafx.application.Platform.runLater(() -> {
                           setText(line.toString());
                        });
                    };
                    line.getMessages().addListener(listener);
                }
            }
        });
        displayedLines.addListener((ListChangeListener<SerialLine>) change -> {
            while (change.next()) {
                if (autoScroll && change.wasAdded()) {
                    int newIndex = change.getFrom();
                    javafx.application.Platform.runLater(() -> {
                        scrollTo(newIndex);
                    });
                }
            }
        });
//        this.setCellFactory(list -> new ListCell<>() {
//
//            @Override
//            protected void updateItem(SerialLine line, boolean empty) {
//                System.out.println("Received Update");
//                super.updateItem(line, empty);
//                if (empty || line == null) {
//                    setText(null);
//                    System.out.println("Null or empty");
//                } else {
//                    System.out.println("line = " + line);
//                    // Automatically update the text when messages in the line change
//                    javafx.application.Platform.runLater(() -> {
//                        setText(line.toString());
//                    });
//                }
//            }
//        });
    }

    public void addLine(SerialLine line) {
        displayedLines.add(line);
    }

    public void clear() {
        displayedLines.clear();
    }

    public void setAutoScroll(boolean autoScroll) {
        this.autoScroll = autoScroll;
    }
}
