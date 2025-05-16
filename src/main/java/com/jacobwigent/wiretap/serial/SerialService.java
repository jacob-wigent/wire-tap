package com.jacobwigent.wiretap.serial;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

public class SerialService {
    private static ArrayList<Integer> baudRates = new ArrayList<>(Arrays.asList(9600, 19200, 38400, 57600, 115200, 230400, 460800, 921600));

    private static SerialPort currentPort;
    private static ArrayList<SerialListener> listeners = new ArrayList<>();

    private static final String MOCK_PORT_NAME = "Emulated";
    private static final EmulatedSerialPort emulatedPort = new EmulatedSerialPort();

    public static void addListener(SerialListener l) {
        listeners.add(l);
    }

    private static long currentConnectionStartTime;

    public static String[] getAvailablePortNames() {
        SerialPort[] serialPorts = SerialPort.getCommPorts();
        String[] portNames = new String[serialPorts.length + 1];

        for (int i = 0; i < serialPorts.length; i++) {
            portNames[i] = serialPorts[i].getSystemPortName();
        }

        portNames[serialPorts.length] = MOCK_PORT_NAME;
        return portNames;
    }

    public static boolean tryConnect() {
        if (MOCK_PORT_NAME.equals(getCurrentPortName())) {
            emulatedPort.openPort();
            currentConnectionStartTime = System.currentTimeMillis();
            return true;
        }

        if (currentPort == null || !currentPort.openPort()) return false;
        addEventListeners(currentPort);
        currentConnectionStartTime = System.currentTimeMillis();
        return true;
    }

    public static boolean tryDisconnect() {
        if (MOCK_PORT_NAME.equals(getCurrentPortName())) {
            return emulatedPort.closePort();
        }

        if (!currentPort.isOpen()) return true;
        return currentPort.closePort();
    }

    public static ArrayList<Integer> getBaudRates() {
        return baudRates;
    }

    public static void setBaudRates (ArrayList<Integer> baudRates) {
        SerialService.baudRates = baudRates;
    }

    private static void addEventListeners(SerialPort port) {
        port.addDataListener(new SerialPortDataListener() {
            @Override
            public int getListeningEvents() {
                return SerialPort.LISTENING_EVENT_DATA_AVAILABLE | SerialPort.LISTENING_EVENT_PORT_DISCONNECTED;
            }

            @Override
            public void serialEvent(SerialPortEvent event) {
                switch (event.getEventType()) {
                    case SerialPort.LISTENING_EVENT_DATA_AVAILABLE:
                        byte[] byteData = new byte[port.bytesAvailable()];
                        int numRead = port.readBytes(byteData, byteData.length);
                        String message = new String(byteData, 0, numRead);
                        if (listeners != null) {
                            for (SerialListener l : listeners) {
                                l.onSerialData(new SerialMessage(message, getElapsedConnectionTime(), LocalDateTime.now()));
                            }
                        }
                        break;

                    case SerialPort.LISTENING_EVENT_PORT_DISCONNECTED:
                        if (listeners != null) {
                            for (SerialListener l : listeners) {
                                l.onDisconnect();
                            }
                        }
                        break;
                }
            }
        });
    }

    public static boolean isConnected() {
        if (MOCK_PORT_NAME.equals(getCurrentPortName())) {
            return emulatedPort.isOpen();
        }
        return currentPort != null && currentPort.isOpen();
    }

    public static int getCurrentBaudRate() {
        if (MOCK_PORT_NAME.equals(getCurrentPortName())) {
            return emulatedPort.getBaudRate();
        }
        return currentPort == null ? 0 : currentPort.getBaudRate();
    }

    public static void selectBaudRate(int baudRate) {
        if (MOCK_PORT_NAME.equals(getCurrentPortName())) {
            emulatedPort.setBaudRate(baudRate);
        } else if (currentPort != null) {
            currentPort.setBaudRate(baudRate);
        }
    }

    public static void selectPort(String portName) {
        if (MOCK_PORT_NAME.equals(portName)) {
            currentPort = null; // Null signals we're using the mock
        } else {
            currentPort = SerialPort.getCommPort(portName);
        }
    }

    public static String getCurrentPortName() {
        if (currentPort != null) return currentPort.getSystemPortName();
        return MOCK_PORT_NAME;
    }

    public static String getConnectionInfo() {
        return getCurrentPortName() + " @ " + getCurrentBaudRate();
    }

    public static SerialPort getCurrentPort() {
        return currentPort;
    }

    public static SerialPort[] getAvailablePorts() {
        return SerialPort.getCommPorts();
    }

    public static long getElapsedConnectionTime() {
        if (MOCK_PORT_NAME.equals(getCurrentPortName())) {
            return System.currentTimeMillis() - currentConnectionStartTime;
        }

        if (currentPort == null || !currentPort.isOpen()) return 0;
        return System.currentTimeMillis() - currentConnectionStartTime;
    }

    public static void kill() {
        if (currentPort != null) currentPort.closePort();
        currentPort = null;
        listeners = null;
    }

    // Simulate data on the emulated port
    public static void fireEmulatedMessage(String msg) {
        if (!MOCK_PORT_NAME.equals(getCurrentPortName())) { return; }
        if (listeners != null) {
            for (SerialListener l : listeners) {
                l.onSerialData(new SerialMessage(msg, getElapsedConnectionTime(), LocalDateTime.now()));
            }
        }
    }
}