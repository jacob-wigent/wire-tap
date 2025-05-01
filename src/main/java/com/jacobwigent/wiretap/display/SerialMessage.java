package com.jacobwigent.wiretap.display;

import java.time.LocalDateTime;

public class SerialMessage {
    final long elapsedMillis;
    final LocalDateTime timestamp;
    final String message;

    public SerialMessage(long elapsedMillis, LocalDateTime timestamp, String message) {
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

    public String getMessage() {
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
