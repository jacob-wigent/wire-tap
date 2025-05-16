package com.jacobwigent.wiretap.serial;

import java.util.Random;

/**
 * Simulates a serial port by generating pseudo-random numeric data
 * at regular intervals for testing purposes.
 */
public class EmulatedSerialPort {
    private boolean open = false;
    private int baudRate = 9600;
    private Thread generatorThread;
    private volatile boolean running = false;
    private int currentValue = 10;
    private final Random random = new Random();

    /**
     * Opens the emulated port and starts data generation.
     * @return true indicating the port is open
     */
    public boolean openPort() {
        open = true;
        running = true;
        startDataThread();
        return true;
    }

    /**
     * Closes the emulated port and stops data generation.
     * Interrupts the data thread if it is running.
     * @return true indicating the port is closed
     */
    public boolean closePort() {
        running = false;
        open = false;
        if (generatorThread != null && generatorThread.isAlive()) {
            generatorThread.interrupt();
        }
        return true;
    }

    /**
     * Checks whether the emulated port is open.
     * @return true if open, false otherwise
     */
    public boolean isOpen() {
        return open;
    }

    /**
     * Gets the baud rate setting for the emulated port.
     * @return baud rate in bits per second
     */
    public int getBaudRate() {
        return baudRate;
    }

    /**
     * Sets the baud rate for the emulated port.
     * @param baudRate baud rate in bits per second
     */
    public void setBaudRate(int baudRate) {
        this.baudRate = baudRate;
    }

    /**
     * Returns the system port name for the emulated port.
     * @return a fixed string "Emulated"
     */
    public String getSystemPortName() {
        return "Emulated";
    }

    /**
     * Returns a descriptive name for the emulated port.
     * @return a fixed string "Emulated Serial Port"
     */
    public String getDescriptivePortName() {
        return "Emulated Serial Port";
    }

    /**
     * Starts a background thread that generates random numeric data
     * every second, simulating data from a serial device.
     */
    private void startDataThread() {
        generatorThread = new Thread(() -> {
            while (running) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    return; // Exit thread if interrupted
                }

                // Randomly adjust currentValue by -2 to +2, clamped between 1 and 20
                int delta = random.nextInt(5) - 2;
                currentValue = Math.max(1, Math.min(20, currentValue + delta));

                // Send the generated value as a serial message with newline
                SerialService.fireEmulatedMessage(currentValue + "\n");
            }
        });
        generatorThread.setDaemon(true);
        generatorThread.start();
    }
}
