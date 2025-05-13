package com.jacobwigent.wiretap.serial;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.LocalDateTime;
import java.util.Objects;

public class SerialLine {
    private final ObservableList<SerialMessage> messages = FXCollections.observableArrayList();
    private LocalDateTime time;
    private Long elapsedMillis;

    public void add(SerialMessage msg) {
        if (this.elapsedMillis == null) {
            this.elapsedMillis = msg.getElapsedMillis();
        }
        if (this.time == null) {
            this.time = msg.getTimestamp();
        }
        messages.add(msg);
    }

    public String getLineText() {
        StringBuilder sb = new StringBuilder();
        for (SerialMessage msg : messages) {
            sb.append(msg.getText());
        }
        return sb.toString();
    }

    public ObservableList<SerialMessage> getMessages() {
        return messages;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SerialLine)) return false;
        SerialLine that = (SerialLine) o;
        return Objects.equals(messages, that.messages) &&
                Objects.equals(time, that.time) &&
                Objects.equals(elapsedMillis, that.elapsedMillis);
    }

    @Override
    public int hashCode() {
        return Objects.hash(messages, time, elapsedMillis);
    }
}
