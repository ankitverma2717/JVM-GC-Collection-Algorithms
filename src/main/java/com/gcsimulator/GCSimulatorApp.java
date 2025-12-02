package com.gcsimulator;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Main JavaFX Application for GC Simulator.
 */
public class GCSimulatorApp extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(
                GCSimulatorApp.class.getResource("/fxml/main.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1400, 900);
        scene.getStylesheets().add(
                GCSimulatorApp.class.getResource("/styles/application.css").toExternalForm());

        stage.setTitle("Java GC Simulator - Visualizing Garbage Collection Algorithms");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
