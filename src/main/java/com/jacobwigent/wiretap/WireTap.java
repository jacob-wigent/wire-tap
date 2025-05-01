package com.jacobwigent.wiretap;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class WireTap extends Application {

    private static final String VERSION = "0.1.0";
    private static final int MIN_WIDTH = 680;
    private static final int MIN_HEIGHT = 420;

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(WireTap.class.getResource("main-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), MIN_WIDTH, MIN_HEIGHT);
        stage.setMinWidth(MIN_WIDTH);
        stage.setMinHeight(MIN_HEIGHT);
        //stage.setMaximized(true);
        stage.setTitle("WireTap " + VERSION);
        stage.getIcons().add(new Image(WireTap.class.getResourceAsStream("/com/jacobwigent/wiretap/icon.png")));
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}