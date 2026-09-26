package com.inventory.controller;

import com.inventory.Main;
import com.inventory.exception.InvalidLoginException;
import com.inventory.model.User;
import com.inventory.repository.UserFileRepository;
import com.inventory.service.AuthService;
import com.inventory.util.Session;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.io.IOException;

// UI controller for login.fxml, delegates to AuthService
public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label statusLabel;

    private final UserFileRepository userFileRepository = new UserFileRepository();
    private final AuthService authService = new AuthService(userFileRepository);

    @FXML
    // login button: authenticate, start session, go to dashboard
    private void handleLogin() {
        statusLabel.setText("");
        try {
            User user = authService.login(usernameField.getText(), passwordField.getText());
            Session.login(user);
            passwordField.clear();
            Main.switchScene("view/dashboard.fxml");
        } catch (InvalidLoginException e) {
            showError(e.getMessage());
        } catch (IOException e) {
            statusLabel.setText("Unable to load dashboard screen.");
        }
    }

    private void showError(String message) {
        statusLabel.setText(message);
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.setTitle("Login Failed");
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}
