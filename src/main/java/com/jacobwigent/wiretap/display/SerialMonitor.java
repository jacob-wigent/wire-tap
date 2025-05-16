package com.jacobwigent.wiretap.display;

import com.jacobwigent.wiretap.serial.SerialMessage;
import javafx.scene.control.TextArea;

/**
 * A custom TextArea used for displaying serial port messages.
 * Supports optional auto-scrolling to keep the newest messages visible,
 * and preserves scroll position when auto-scroll is disabled.
 */
public class SerialMonitor extends TextArea {

    private boolean autoScroll = true;

    /**
     * Appends the given serial message text to the monitor.
     * If autoScroll is disabled, preserves the current scroll position and text selection.
     *
     * @param msg the SerialMessage to display
     */
    public void print(SerialMessage msg) {
        // Save current scroll position and text selection before appending
        double pos = this.getScrollTop();
        int anchor = this.getAnchor();
        int caret = this.getCaretPosition();

        this.appendText(msg.getText());

        // If autoScroll is off, restore previous scroll and selection
        if (!autoScroll) {
            this.setScrollTop(pos);
            this.selectRange(anchor, caret);
        }
    }

    /**
     * Enables or disables automatic scrolling to the bottom when new text is appended.
     *
     * @param autoScroll true to enable auto-scroll; false to disable
     */
    public void setAutoScroll(boolean autoScroll) {
        this.autoScroll = autoScroll;
    }

    /**
     * Clears all text from the monitor.
     */
    public void clear() {
        super.clear();
    }
}
