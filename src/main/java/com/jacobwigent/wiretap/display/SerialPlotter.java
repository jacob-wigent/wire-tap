package com.jacobwigent.wiretap.display;

import com.jacobwigent.wiretap.serial.SerialMessage;
import javafx.scene.chart.Axis;
import javafx.scene.chart.Chart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.TextArea;

import javafx.scene.chart.XYChart;

public class SerialPlotter extends LineChart<Number, Number> {

    private final XYChart.Series<Number, Number> series;
    private boolean autoScroll = true;
    private long pointIndex = 0;

    public SerialPlotter() {
        super(new NumberAxis(), new NumberAxis());
        this.series = new XYChart.Series<>();
        getData().add(series);
        setAnimated(false); // Improves performance for real-time data
        setLegendVisible(false);
    }

    public void setAutoScroll(boolean autoScroll) {
        this.autoScroll = autoScroll;
    }

    public void clear() {
        series.getData().clear();
        pointIndex = 0;
    }

    public void addData(SerialMessage message) {
        try {
            String text = message.getText().trim();
            if (!text.isEmpty()) {
                double y = Double.parseDouble(text);
                double x = pointIndex++;//message.getElapsedMillis() / 1000.0; // Or use pointIndex++
                series.getData().add(new XYChart.Data<>(x, y));
            }
        } catch (NumberFormatException e) {
            // Ignore non-numeric messages
        }
    }

}


