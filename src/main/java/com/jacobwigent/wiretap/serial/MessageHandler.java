package com.jacobwigent.wiretap.serial;

import com.jacobwigent.wiretap.display.SerialMonitor;
import com.jacobwigent.wiretap.display.SerialPlotter;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles incoming serial messages by updating the serial monitor and plotter.
 * Supports freezing of the display to buffer messages temporarily.
 */
public class MessageHandler implements SerialListener {

    private final SerialMonitor monitor;
    private final SerialPlotter plotter;

    private boolean frozen = false;

    private final List<SerialMessage> messages = new ArrayList<>();
    private final List<SerialMessage> messageBuffer = new ArrayList<>();
    private int lineCount = 0;
    private long lastMessageTime = 0;
    private int[] deltaTimes = new int[10];

    /**
     * Constructs a MessageHandler with references to the serial monitor and plotter.
     * Registers itself as a listener to the SerialService.
     *
     * @param monitor the SerialMonitor instance to update with text messages
     * @param plotter the SerialPlotter instance to update with numeric data
     */
    public MessageHandler(SerialMonitor monitor, SerialPlotter plotter) {
        this.monitor = monitor;
        this.plotter = plotter;
        SerialService.addListener(this);
    }

    /**
     * Sets whether the handler should freeze the display updates.
     * When unfrozen, any buffered messages will be flushed to the display.
     *
     * @param frozen true to freeze display updates, false to resume
     */
    public void setFreeze(boolean frozen) {
        this.frozen = frozen;
        if (!frozen) {
            emptyBuffer();
        }
    }

    /**
     * Flushes the message buffer by printing and plotting all buffered messages on the JavaFX application thread.
     */
    private void emptyBuffer() {
        for (SerialMessage msg : messageBuffer) {
            javafx.application.Platform.runLater(() -> {
                monitor.print(msg);
                plotter.addData(msg);
            });
        }
        messageBuffer.clear();
    }

    /**
     * Callback invoked when new serial data arrives.
     * Updates internal state, displays text and plots numeric data unless frozen.
     * Tracks time intervals between messages.
     *
     * @param msg the incoming SerialMessage
     */
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

        // Track message interval times to calculate average rate
        long currentMsgTime = msg.getElapsedMillis();
        if (lastMessageTime == 0) {
            lastMessageTime = currentMsgTime;
            return;
        }
        int currentDeltaTime = (int) (currentMsgTime - lastMessageTime);
        // Shift previous delta times left and add new delta at the end
        System.arraycopy(deltaTimes, 1, deltaTimes, 0, deltaTimes.length - 1);
        deltaTimes[deltaTimes.length - 1] = currentDeltaTime;
        lastMessageTime = currentMsgTime;
    }

    /**
     * Callback invoked on serial disconnect. Currently no action taken.
     */
    @Override
    public void onDisconnect() {}

    /**
     * Returns the total number of messages received.
     *
     * @return message count
     */
    public int getMessageCount() {
        return messages.size();
    }

    /**
     * Returns the total number of lines received (counting newlines in messages).
     *
     * @return line count
     */
    public int getLineCount() {
        return lineCount;
    }

    /**
     * Calculates the average time interval between recent messages in milliseconds.
     *
     * @return average interval in ms, or 0 if no data
     */
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

    /**
     * Clears all stored messages, buffers, counters, and resets the monitor and plotter display.
     */
    public void flush() {
        messages.clear();
        messageBuffer.clear();
        lineCount = 0;
        deltaTimes = new int[10];
        lastMessageTime = 0;
        javafx.application.Platform.runLater(() -> {
            monitor.clear();
            plotter.reset();
        });
    }
}
