package com.jacobwigent.wiretap.display;

import com.jacobwigent.wiretap.serial.SerialMessage;
import javafx.scene.control.TextArea;

public class SerialMonitor extends TextArea {

    private boolean autoScroll = true;

    public void print(SerialMessage msg) {
        double pos = this.getScrollTop();
        int anchor = this.getAnchor();
        int caret = this.getCaretPosition();

        this.appendText(msg.getText());

        if (!autoScroll) {
            this.setScrollTop(pos);
            this.selectRange(anchor, caret);
        }
    }

    public void setAutoScroll(boolean autoScroll) {
        this.autoScroll = autoScroll;
    }

    public void clear() {
        super.clear();
    }
}
