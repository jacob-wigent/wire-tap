package com.jacobwigent.wiretap.serial;

import com.fazecast.jSerialComm.SerialPort;

import java.util.Arrays;

public class SerialService {
    public static final int[] baudRates = {9600, 19200, 38400, 57600, 115200, 230400, 460800, 921600};

    public static final String[] getAvaiablePorts() {
        SerialPort[] serialPorts = SerialPort.getCommPorts();
        String[] portNames = new String[serialPorts.length];
        for (int i = 0; i < serialPorts.length; i++) {
            portNames[i] = serialPorts[i].getSystemPortName();
        }
        return portNames;
    }

}
