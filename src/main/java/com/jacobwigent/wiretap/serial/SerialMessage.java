package com.jacobwigent.wiretap.serial;

import java.time.LocalDateTime;

public class SerialMessage {
    private final long elapsedMillis;
    private final LocalDateTime timestamp;
    private final String message;

    protected SerialMessage(long elapsedMillis, LocalDateTime timestamp, String message) {
        this.elapsedMillis = elapsedMillis;
        this.timestamp = timestamp;
        this.message = message;
    }

    public long getElapsedMillis() {
        return elapsedMillis;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getText() {
        return message;
    }

    public String getFormattedMessage() {
        return "[" + formatTime(elapsedMillis) + "] " + message;
    }

    public static String formatTime(long milliseconds) {
        int totalSeconds = (int) (milliseconds / 1000);
        int minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        long remainingMilliseconds = milliseconds % 1000;

        return String.format("%d:%d:%d", minutes, seconds, remainingMilliseconds);
    }
}
