package com.jacobwigent.wiretap.serial;

import java.util.Random;

public class EmulatedSerialPort {
    private boolean open = false;
    private int baudRate = 9600;
    private Thread generatorThread;
    private volatile boolean running = false;
    private int currentValue = 10;
    private Random random = new Random();

    public boolean openPort() {
        open = true;
        running = true;
        startDataThread();
        return true;
    }

    public boolean closePort() {
        running = false;
        open = false;
        if (generatorThread != null && generatorThread.isAlive()) {
            generatorThread.interrupt();
        }
        return true;
    }

    public boolean isOpen() {
        return open;
    }

    public int getBaudRate() {
        return baudRate;
    }

    public void setBaudRate(int baudRate) {
        this.baudRate = baudRate;
    }

    public String getSystemPortName() {
        return "Emulated";
    }

    public String getDescriptivePortName() {
        return "Emulated Serial Port";
    }

    private void startDataThread() {
        generatorThread = new Thread(() -> {
            while (running) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    return;
                }

                // Change value by -2, -1, 0, 1, or 2
                int delta = random.nextInt(5) - 2;
                currentValue = Math.max(1, Math.min(20, currentValue + delta));
                SerialService.fireEmulatedMessage(String.valueOf(currentValue) + "\n");
            }
        });
        generatorThread.setDaemon(true);
        generatorThread.start();
    }
}
