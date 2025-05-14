package com.jacobwigent.wiretap.serial;

import com.jacobwigent.wiretap.display.SerialMonitor;

import java.util.ArrayList;
import java.util.List;

public class MessageHandler implements SerialListener {

    private final SerialMonitor monitor;

    private boolean frozen = false;

    private final List<SerialMessage> messages = new ArrayList<>();
    private final List<SerialMessage> messageBuffer = new ArrayList<>();
    private int lineCount = 0;

    public MessageHandler(SerialMonitor monitor) {
        this.monitor = monitor;
        SerialService.addListener(this);
    }

    public void setFreeze(boolean frozen) {
        this.frozen = frozen;
        if (!frozen) {
            emptyBuffer();
        }
    }

    private void emptyBuffer() {
        for (SerialMessage msg : messageBuffer) {
            javafx.application.Platform.runLater(() -> {
                monitor.print(msg);
            });
        }
        messageBuffer.clear();
    }

    @Override
    public void onSerialData(SerialMessage msg) {
        messages.add(msg);
        lineCount += (int) msg.getText().chars().filter(ch -> ch == '\n').count();
        if (frozen) {
            messageBuffer.add(msg);
        } else {
            javafx.application.Platform.runLater(() -> {
                monitor.print(msg);
            });
        }
    }

    @Override
    public void onDisconnect() {}

    public int getMessageCount() {
        return messages.size();
    }

    public int getLineCount() {
        return lineCount;
    }

    public void flush() {
        messages.clear();
        messageBuffer.clear();
        lineCount = 0;
        monitor.clear();
    }
}
