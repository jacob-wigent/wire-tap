package com.jacobwigent.wiretap.serial;

import com.jacobwigent.wiretap.display.SerialMonitor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MessageHandler implements SerialListener {

    private final SerialMonitor monitor;

    private boolean frozen = false;

    private final List<SerialMessage> allMessages = new ArrayList<>();
    private final List<SerialMessage> freezeBuffer = new ArrayList<>();
    private final List<SerialMessage> accessableMessages = new ArrayList<>();

    private final List<SerialLine> accessibleLines = new ArrayList<>();
    private SerialLine currentLineBuffer = null;

    public MessageHandler(SerialMonitor monitor) {
        this.monitor = monitor;
        SerialService.addListener(this);
    }

    @Override
    public void onSerialData(SerialMessage msg) {
        allMessages.add(msg);

        if (frozen) {
            freezeBuffer.add(msg);
        } else {
            accessableMessages.add(msg);
            handleNewMessage(msg);
        }
    }

    private void handleNewMessage(SerialMessage msg) {
        String text = msg.getText();
        System.out.println("text = " + text);

        if (currentLineBuffer == null) {
            currentLineBuffer = new SerialLine();
            accessibleLines.add(currentLineBuffer);
            javafx.application.Platform.runLater(() -> monitor.addLine(currentLineBuffer));
        }

        if (text == null || text.isEmpty()) {
            System.out.println("Ignored empty message");
            return;
        }

        currentLineBuffer.add(msg); // Observable list update UI automatically when message is added

        if (text.endsWith("\n")) {
            currentLineBuffer = null;
        }
    }


    public void setFreeze(boolean frozen) {
        this.frozen = frozen;
        if (!frozen) {
            emptyBuffer();
        }
    }

    private void emptyBuffer() {
        for (SerialMessage msg : freezeBuffer) {
            handleNewMessage(msg);
            accessableMessages.add(msg);
        }
        freezeBuffer.clear();
    }

    @Override
    public void onDisconnect() {}

    public int getAllMessageCount() {
        return allMessages.size();
    }

    public int getLineCount() {
        return accessibleLines.size();
    }
}
