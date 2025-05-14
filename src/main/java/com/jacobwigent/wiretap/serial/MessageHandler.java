package com.jacobwigent.wiretap.serial;

import com.jacobwigent.wiretap.display.SerialMonitor;
import com.jacobwigent.wiretap.display.SerialPlotter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MessageHandler implements SerialListener {

    private final SerialMonitor monitor;
    private final SerialPlotter plotter;

    private boolean frozen = false;

    private final List<SerialMessage> messages = new ArrayList<>();
    private final List<SerialMessage> messageBuffer = new ArrayList<>();
    private int lineCount = 0;
    private long lastMessageTime = 0;
    private int[] deltaTimes = new int[10];

    public MessageHandler(SerialMonitor monitor, SerialPlotter plotter) {
        this.monitor = monitor;
        this.plotter = plotter;
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
                plotter.addData(msg);
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
                plotter.addData(msg);
            });
        }

        long currentMsgTime = msg.getElapsedMillis();
        if (lastMessageTime == 0) {
            lastMessageTime = currentMsgTime;
            return;
        }
        int currentDeltaTime = (int) (currentMsgTime - lastMessageTime);
        for (int i = 0; i < deltaTimes.length - 1; i++) {
            deltaTimes[i] = deltaTimes[i + 1];
        }
        deltaTimes[deltaTimes.length - 1] = currentDeltaTime;
        lastMessageTime = currentMsgTime;
    }

    @Override
    public void onDisconnect() {}

    public int getMessageCount() {
        return messages.size();
    }

    public int getLineCount() {
        return lineCount;
    }

    public int getAverageRate() {
        int sum = 0;
        int count = 0;
        for (int i : deltaTimes) {
            if (i > 0) {
                sum += i;
                count++;
            }
        }
        return (count == 0) ? 0 : sum / count;
    }

    public void flush() {
        messages.clear();
        messageBuffer.clear();
        lineCount = 0;
        deltaTimes = new int[10];
        javafx.application.Platform.runLater(() -> {
            monitor.clear();
            plotter.reset();
        });
    }
}
