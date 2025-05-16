package com.jacobwigent.wiretap.serial;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

/**
 * Provides static methods for managing serial port connections,
 * including real and emulated ports, and dispatching serial data events
 * to registered listeners.
 * <br><br>
 * This service supports connecting to real hardware serial ports
 * as well as an emulated serial port for testing without hardware.
 * It manages listeners implementing {@link SerialListener} to notify
 * about incoming data and disconnection events.
 */
public class SerialService {

    private static ArrayList<Integer> baudRates = new ArrayList<>(Arrays.asList(9600, 19200, 38400, 57600, 115200, 230400, 460800, 921600));

    private static SerialPort currentPort;
    private static ArrayList<SerialListener> listeners = new ArrayList<>();

    private static final String MOCK_PORT_NAME = "Emulated";
    private static final EmulatedSerialPort emulatedPort = new EmulatedSerialPort();

    private static long currentConnectionStartTime;

    /**
     * Adds a listener to receive serial data and disconnect events.
     *
     * @param l the listener to add
     */
    public static void addListener(SerialListener l) {
        listeners.add(l);
    }

    /**
     * Returns the list of available serial port names,
     * including the emulated port.
     *
     * @return array of available port names as strings
     */
    public static String[] getAvailablePortNames() {
        SerialPort[] serialPorts = SerialPort.getCommPorts();
        String[] portNames = new String[serialPorts.length + 1];

        for (int i = 0; i < serialPorts.length; i++) {
            portNames[i] = serialPorts[i].getSystemPortName();
        }

        portNames[serialPorts.length] = MOCK_PORT_NAME;
        return portNames;
    }

    /**
     * Attempts to open a connection to the currently selected port.
     *
     * @return true if the connection was successfully opened, false otherwise
     */
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

    /**
     * Attempts to close the current serial port connection.
     *
     * @return true if the port was closed successfully or was already closed
     */
    public static boolean tryDisconnect() {
        if (MOCK_PORT_NAME.equals(getCurrentPortName())) {
            return emulatedPort.closePort();
        }

        if (!currentPort.isOpen()) return true;
        return currentPort.closePort();
    }

    /**
     * Returns the list of supported baud rates.
     *
     * @return list of supported baud rates as integers
     */
    public static ArrayList<Integer> getBaudRates() {
        return baudRates;
    }

    /**
     * Sets the list of supported baud rates.
     *
     * @param baudRates list of baud rates to support
     */
    public static void setBaudRates(ArrayList<Integer> baudRates) {
        SerialService.baudRates = baudRates;
    }

    /**
     * Selects the port to connect to by name.
     * If the name is "Emulated", selects the emulated port.
     *
     * @param portName the system name of the port to select
     */
    public static void selectPort(String portName) {
        if (MOCK_PORT_NAME.equals(portName)) {
            currentPort = null; // Null signals using emulated port
        } else {
            currentPort = SerialPort.getCommPort(portName);
        }
    }

    /**
     * Returns the name of the currently selected port,
     * or "Emulated" if using the emulated port.
     *
     * @return the current port name
     */
    public static String getCurrentPortName() {
        if (currentPort != null) return currentPort.getSystemPortName();
        return MOCK_PORT_NAME;
    }

    /**
     * Returns a string describing the current connection,
     * including port name and baud rate.
     *
     * @return connection info string
     */
    public static String getConnectionInfo() {
        return getCurrentPortName() + " @ " + getCurrentBaudRate();
    }

    /**
     * Returns the currently selected baud rate.
     *
     * @return baud rate in bits per second
     */
    public static int getCurrentBaudRate() {
        if (MOCK_PORT_NAME.equals(getCurrentPortName())) {
            return emulatedPort.getBaudRate();
        }
        return currentPort == null ? 0 : currentPort.getBaudRate();
    }

    /**
     * Sets the baud rate for the current connection.
     *
     * @param baudRate the baud rate to set
     */
    public static void selectBaudRate(int baudRate) {
        if (MOCK_PORT_NAME.equals(getCurrentPortName())) {
            emulatedPort.setBaudRate(baudRate);
        } else if (currentPort != null) {
            currentPort.setBaudRate(baudRate);
        }
    }

    /**
     * Returns whether there is an active connection to a serial port.
     *
     * @return true if connected, false otherwise
     */
    public static boolean isConnected() {
        if (MOCK_PORT_NAME.equals(getCurrentPortName())) {
            return emulatedPort.isOpen();
        }
        return currentPort != null && currentPort.isOpen();
    }

    /**
     * Returns an array of all available serial ports detected by the system.
     *
     * @return array of available SerialPort objects
     */
    public static SerialPort[] getAvailablePorts() {
        return SerialPort.getCommPorts();
    }

    /**
     * Returns the elapsed time in milliseconds since the current connection was established.
     *
     * @return elapsed connection time in milliseconds, or 0 if not connected
     */
    public static long getElapsedConnectionTime() {
        if (MOCK_PORT_NAME.equals(getCurrentPortName())) {
            return System.currentTimeMillis() - currentConnectionStartTime;
        }

        if (currentPort == null || !currentPort.isOpen()) return 0;
        return System.currentTimeMillis() - currentConnectionStartTime;
    }

    /**
     * Closes the current port and clears listeners.
     */
    public static void kill() {
        if (currentPort != null) currentPort.closePort();
        currentPort = null;
        listeners = null;
    }

    /**
     * Forcibly sends a simulated serial message to all registered listeners.
     * Only works when using the emulated port.
     *
     * @param msg the message text to send
     */
    public static void fireEmulatedMessage(String msg) {
        if (!MOCK_PORT_NAME.equals(getCurrentPortName())) { return; }
        if (listeners != null) {
            for (SerialListener l : listeners) {
                l.onSerialData(new SerialMessage(msg, getElapsedConnectionTime(), LocalDateTime.now()));
            }
        }
    }

    /**
     * Adds a data listener to a SerialPort instance, forwarding data events
     * and disconnect events to registered listeners.
     *
     * @param port the SerialPort to add event listeners to
     */
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
}
