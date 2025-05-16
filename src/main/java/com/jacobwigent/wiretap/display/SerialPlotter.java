package com.jacobwigent.wiretap.display;

import com.jacobwigent.wiretap.serial.SerialMessage;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;

/**
 * A LineChart specialized for plotting numeric data from serial messages in real-time.
 * Supports auto-scrolling on the x-axis based on elapsed time from messages.
 */
public class SerialPlotter extends LineChart<Number, Number> {

    private final NumberAxis xAxis;
    private final NumberAxis yAxis;
    private final XYChart.Series<Number, Number> series;
    private boolean indexed = false;
    private boolean autoScroll = true;
    private long pointIndex = 0;
    private long lastMessageTime = 0;

    /**
     * Constructs a SerialPlotter with configured axes and series for real-time plotting.
     */
    public SerialPlotter() {
        super(new NumberAxis(), new NumberAxis());
        xAxis = (NumberAxis) this.getXAxis();
        yAxis = (NumberAxis) this.getYAxis();
        this.series = new XYChart.Series<>();
        getData().add(series);
        setAnimated(false); // Improves performance for real-time data
        setLegendVisible(false);
        xAxis.setAutoRanging(false);
        xAxis.setTickUnit(1.0);
    }

    /**
     * Enables or disables automatic scrolling of the x-axis as new data arrives.
     * @param autoScroll true to enable auto-scroll, false to disable
     */
    public void setAutoScroll(boolean autoScroll) {
        this.autoScroll = autoScroll;
    }

    /**
     * Clears all plotted data and resets the x-axis lower bound.
     */
    public void clear() {
        series.getData().clear();
        pointIndex = 0;
        // Reset x-axis lower bound to last known time to avoid jump
        xAxis.setLowerBound(lastMessageTime / 1000.0);
    }

    /**
     * Resets the plotter state including clearing data and resetting time tracking.
     */
    public void reset() {
        lastMessageTime = 0;
        this.clear();
    }

    /**
     * Adds a new data point from a serial message to the plot.
     * Expects the message text to be parseable as a double.
     * Ignores messages that cannot be parsed.
     *
     * @param message the SerialMessage containing numeric data and timestamp
     */
    public void addData(SerialMessage message) {
        try {
            String text = message.getText().trim();
            if (!text.isEmpty()) {
                double y = Double.parseDouble(text);
                long currentMessageTime = message.getElapsedMillis();

                // Update x-axis upper bound for auto-scrolling effect
                if (currentMessageTime > lastMessageTime) {
                    xAxis.setUpperBound(currentMessageTime / 1000.0);
                }
                lastMessageTime = currentMessageTime;

                double x = lastMessageTime / 1000.0;
                XYChart.Data<Number, Number> data = new XYChart.Data<>(x, y);
                series.getData().add(data);

                // Add a click listener for future use
                data.getNode().setOnMouseClicked(e ->
                        System.out.println("Click on data (" + data.getXValue() + "," + data.getYValue() + ")"));
            }
        } catch (NumberFormatException e) {
            // Ignore messages that are not numeric, no plot update needed
        }
    }

}