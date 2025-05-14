package com.jacobwigent.wiretap.serial;

import java.time.LocalDateTime;

public class SerialMessage {
    private final String text;
    private final long elapsedMillis;
    private final LocalDateTime timestamp;

    protected SerialMessage(String text, long elapsedMillis, LocalDateTime timestamp) {
        this.text = text;//.replace("\r\n", "\n").replace("\r", "\n"); // Normalize line endings
        this.elapsedMillis = elapsedMillis;
        this.timestamp = timestamp;
    }

    public long getElapsedMillis() {
        return elapsedMillis;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getText() {
        return text;
    }
}
