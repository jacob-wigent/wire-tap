package com.jacobwigent.wiretap.serial;

import com.jacobwigent.wiretap.display.SerialMonitor;

import java.util.ArrayList;
import java.util.List;

public class MessageHandler implements SerialListener {

    private final SerialMonitor monitor;

    private boolean frozen = false;

    private final List<SerialMessage> allMessages = new ArrayList<>();
    private final List<SerialMessage> freezeBuffer = new ArrayList<>();
    private final List<SerialMessage> accessibleMessages = new ArrayList<>();

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
            accessibleMessages.add(msg);
            handleNewMessage(msg);
        }
    }

    private void handleNewMessage(SerialMessage msg) {
        String text = msg.getText();
        if (text.endsWith("\n")) {
            currentLineBuffer = null;
            return;
        }

        if (currentLineBuffer == null) {
            currentLineBuffer = new SerialLine();
            accessibleLines.add(currentLineBuffer);
            javafx.application.Platform.runLater(() -> monitor.addLine(currentLineBuffer));
        }

        currentLineBuffer.add(msg);
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
            accessibleMessages.add(msg);
        }
        freezeBuffer.clear();
    }

    public void reset() {
        accessibleMessages.clear();
        accessibleLines.clear();
        freezeBuffer.clear();
        currentLineBuffer = null;
    }

    @Override
    public void onDisconnect() {}

    public int getAllMessageCount() {
        return allMessages.size();
    }

    public int getAccessibleMessageCount() {
        return accessibleMessages.size();
    }

    public int getLineCount() {
        return accessibleLines.size();
    }
}
