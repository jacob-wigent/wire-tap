package com.jacobwigent.wiretap.serial;

public interface SerialListener {
    void onSerialData(String data);
    void onDisconnect();
}
