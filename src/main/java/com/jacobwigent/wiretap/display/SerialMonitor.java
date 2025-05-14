package com.jacobwigent.wiretap.display;

import com.jacobwigent.wiretap.serial.SerialMessage;
import javafx.scene.control.TextArea;

public class SerialMonitor extends TextArea {

    public void print(SerialMessage msg) {
        this.appendText(msg.getText());
    }

    public void clear() {
        super.clear();
    }
}
