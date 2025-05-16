package com.jacobwigent.wiretap.serial;

import java.time.LocalDateTime;

/**
 * Represents a message received from a serial port.
 * Contains the message text, the elapsed time since connection started,
 * and the timestamp when the message was received.
 */
public class SerialMessage {

    private final String text;
    private final long elapsedMillis;
    private final LocalDateTime timestamp;

    /**
     * Constructs a new SerialMessage.
     *
     * @param text the content of the serial message
     * @param elapsedMillis the elapsed time in milliseconds since connection start
     * @param timestamp the timestamp when the message was received
     */
    protected SerialMessage(String text, long elapsedMillis, LocalDateTime timestamp) {
        this.text = text; // Consider normalizing line endings if needed
        this.elapsedMillis = elapsedMillis;
        this.timestamp = timestamp;
    }

    /**
     * Gets the elapsed time in milliseconds since the connection started when this message was received.
     *
     * @return elapsed time in milliseconds
     */
    public long getElapsedMillis() {
        return elapsedMillis;
    }

    /**
     * Gets the timestamp when this message was received.
     *
     * @return the timestamp as a LocalDateTime object
     */
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    /**
     * Gets the text content of this serial message.
     *
     * @return the message text
     */
    public String getText() {
        return text;
    }
}
