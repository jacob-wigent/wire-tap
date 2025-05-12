package com.jacobwigent.wiretap.display;

import com.jacobwigent.wiretap.serial.SerialLine;
import com.jacobwigent.wiretap.serial.SerialMessage;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;

import java.util.ArrayList;

public class SerialMonitor extends TextArea {

    private final ArrayList<SerialMessage> currentLineBuffer = new ArrayList<>();
    private final ArrayList<SerialLine> lines = new ArrayList<>();

    public void print(Object obj) {
        this.appendText(obj.toString());
    }

    public void print(SerialMessage msg) {
        currentLineBuffer.add(msg);
        String text = msg.getText();
        if (text.endsWith("\n")) {
            lines.add(new SerialLine(currentLineBuffer));
            currentLineBuffer.clear();
        }
        this.appendText(text);
    }

    public void clear() {
        super.clear();
    }

    public int getLineCount() {
        return lines.size();
    }
}
