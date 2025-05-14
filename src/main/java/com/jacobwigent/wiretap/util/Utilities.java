package com.jacobwigent.wiretap.util;

public class Utilities {
    public static String formatTime(long milliseconds) {
        int totalSeconds = (int) (milliseconds / 1000);
        int minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        long remainingMilliseconds = milliseconds % 1000;

        return String.format("%d:%d:%d", minutes, seconds, remainingMilliseconds);
    }
}
