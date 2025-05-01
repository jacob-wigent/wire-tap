package com.jacobwigent.wiretap.display;

import com.jacobwigent.wiretap.serial.SerialService;
import javafx.scene.control.TextArea;

import java.time.LocalDateTime;
import java.util.ArrayList;

public class SerialMonitor extends TextArea {

    private boolean frozen = false;

    private final ArrayList<SerialMessage> messages = new ArrayList<>();
    private final ArrayList<SerialMessage> messageBuffer = new ArrayList<>();

    public void print(Object obj) {
        SerialMessage msg = new SerialMessage(SerialService.getElapsedConnectionTime(), LocalDateTime.now(), obj.toString());
        messages.add(msg);
        if (frozen) {
            messageBuffer.add(msg);
        } else {
            this.appendText(msg.getMessage());
        }
    }

    public void setFreeze(boolean frozen) {
        this.frozen = frozen;
        if (!frozen) {
            emptyBuffer();
        }
    }

    private void emptyBuffer() {
        if (messageBuffer.isEmpty()) { return; }
        for (SerialMessage msg : messageBuffer) {
            this.appendText(msg.getMessage());
        }
        messageBuffer.clear();
    }

    public void clear() {
        super.clear();
    }
}
