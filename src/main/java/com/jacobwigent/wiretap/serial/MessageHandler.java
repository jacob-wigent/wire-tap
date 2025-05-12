package com.jacobwigent.wiretap.serial;

import com.jacobwigent.wiretap.display.SerialMonitor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MessageHandler implements SerialListener {

    private final SerialMonitor monitor;

    private boolean frozen = false;

    private final List<SerialMessage> messages = new ArrayList<>();
    private final List<SerialMessage> messageBuffer = new ArrayList<>();

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
            monitor.print(msg);
        }
        messageBuffer.clear();
    }

    @Override
    public void onSerialData(String data) {
        SerialMessage msg = new SerialMessage(SerialService.getElapsedConnectionTime(), LocalDateTime.now(), data);
        messages.add(msg);
        if (frozen) {
            messageBuffer.add(msg);
        } else {
            monitor.print(msg);
        }
    }

    @Override
    public void onDisconnect() {}

    public int getMessageCount() {
        return messages.size();
    }
}
