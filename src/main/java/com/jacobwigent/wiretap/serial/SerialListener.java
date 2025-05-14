package com.jacobwigent.wiretap.serial;

public interface SerialListener {
    void onSerialData(SerialMessage msg);
    void onDisconnect();
}
