package com.jacobwigent.wiretap.serial;

import java.time.LocalDateTime;

public class SerialMessage {
    private final long elapsedMillis;
    private final LocalDateTime timestamp;
    private final String text;

    protected SerialMessage(long elapsedMillis, LocalDateTime timestamp, String text) {
        this.elapsedMillis = elapsedMillis;
        this.timestamp = timestamp;
        this.text = text.replace("\r\n", "\n").replace("\r", "\n");;
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

    @Override
    public String toString() {
        return "[" + formatTime(elapsedMillis) + "] " + text;
    }

    public static String formatTime(long milliseconds) {
        int totalSeconds = (int) (milliseconds / 1000);
        int minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        long remainingMilliseconds = milliseconds % 1000;

        return String.format("%d:%d:%d", minutes, seconds, remainingMilliseconds);
    }
}
