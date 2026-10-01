package com.inventory;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {

    private static final double WINDOW_WIDTH = 1100;
    private static final double WINDOW_HEIGHT = 760;

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;
        primaryStage.setTitle("Inventory Management & POS System");
        loadAppIcons(primaryStage);

        primaryStage.setWidth(WINDOW_WIDTH);
        primaryStage.setHeight(WINDOW_HEIGHT);
        primaryStage.setResizable(true);

        switchScene("view/login.fxml");

        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    /**
     * Sets the window / taskbar icon. Several sizes are supplied so the OS can
     * pick the sharpest one (16px title bar, 32px taskbar, 256px Alt-Tab...).
     * Files live in src/main/resources/com/inventory/view/images/.
     */
    private static void loadAppIcons(Stage stage) {
        for (int size : new int[] {16, 24, 32, 48, 64, 128, 256, 512}) {
            java.net.URL url = Main.class.getResource("view/images/logo-" + size + ".png");
            if (url != null) {
                stage.getIcons().add(new Image(url.toExternalForm()));
            }
        }
    }

    public static void switchScene(String fxmlFile) throws IOException {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource(fxmlFile));
            Parent root = loader.load();

            Scene currentScene = primaryStage.getScene();
            if (currentScene == null) {
                Scene scene = new Scene(root);
                scene.getStylesheets().add(
                        Main.class.getResource("view/style.css").toExternalForm());
                primaryStage.setScene(scene);
            } else {
                currentScene.setRoot(root);
            }
        } catch (IOException e) {
            showLoadError(fxmlFile, e);
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