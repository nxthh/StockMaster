package com.inventory;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

import java.io.IOException;

// JavaFX entry point (inheritance: Main is-a Application)
public class Main extends Application {

    // fixed window size, same for every screen
    private static final double WINDOW_WIDTH = 1100;
    private static final double WINDOW_HEIGHT = 760;

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;
        primaryStage.setTitle("Inventory Management & POS System");

        primaryStage.setWidth(WINDOW_WIDTH);
        primaryStage.setHeight(WINDOW_HEIGHT);

        switchScene("view/login.fxml"); // first screen shown

        primaryStage.centerOnScreen();
        primaryStage.setResizable(true);
        primaryStage.show();
    }

    // navigation: swaps the current screen on the shared stage
    public static void switchScene(String fxmlFile) throws IOException {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource(fxmlFile));
            Scene scene = new Scene(loader.load());

            scene.getStylesheets().add(Main.class.getResource("view/style.css").toExternalForm());

            primaryStage.setScene(scene);

            // keep the fixed size after every screen switch
            primaryStage.setWidth(WINDOW_WIDTH);
            primaryStage.setHeight(WINDOW_HEIGHT);
        } catch (IOException e) {
            showLoadError(fxmlFile, e); // surfaces load failures on screen
            throw e;
        }
    }

    private static void showLoadError(String fxmlFile, Exception e) {
        Throwable cause = (e.getCause() != null) ? e.getCause() : e;
        Alert alert = new Alert(Alert.AlertType.ERROR,
                "Could not open this screen (" + fxmlFile + ").\n\n"
                        + cause.getClass().getSimpleName()
                        + (cause.getMessage() != null ? ": " + cause.getMessage() : ""),
                ButtonType.OK);
        alert.setTitle("Navigation Error");
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
