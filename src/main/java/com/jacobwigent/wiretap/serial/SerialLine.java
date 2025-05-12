package com.jacobwigent.wiretap.serial;

import java.time.LocalDateTime;
import java.util.ArrayList;

public class SerialLine {
    private final ArrayList<SerialMessage> messages;
    private final LocalDateTime time;
    private final long elapsedMillis;

    public SerialLine(ArrayList<SerialMessage> messages) {
        this.messages = messages;
        SerialMessage firstMessage = messages.get(0);
        this.time = firstMessage.getTimestamp();
        this.elapsedMillis = firstMessage.getElapsedMillis();
    }
}
