package com.jacobwigent.wiretap;

import javafx.scene.control.TextArea;

public class SerialMonitor extends TextArea {
    public void print(Object obj) {
        this.appendText(obj.toString());
    }
    public void println(Object obj) {
        this.appendText(obj.toString() + "\n");
    }
    public void clear() {
        super.clear();
    }
}
