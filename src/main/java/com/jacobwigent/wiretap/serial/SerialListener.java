package com.jacobwigent.wiretap.serial;

/**
 * Listener interface for receiving serial communication events.
 */
public interface SerialListener {

    /**
     * Called when new serial data is received.
     *
     * @param msg the received serial message
     */
    void onSerialData(SerialMessage msg);

    /**
     * Called when the serial connection is disconnected.
     */
    void onDisconnect();
}
