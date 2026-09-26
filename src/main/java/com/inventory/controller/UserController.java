package com.inventory.controller;

import com.inventory.Main;
import com.inventory.exception.DuplicateUsernameException;
import com.inventory.exception.InvalidUserException;
import com.inventory.model.Cashier;
import com.inventory.model.Role;
import com.inventory.model.User;
import com.inventory.repository.UserFileRepository;
import com.inventory.service.UserService;
import com.inventory.util.Session;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

// UI controller for users.fxml: admin-only cashier account management
public class UserController {

    @FXML
    private Label roleLabel;

    @FXML
    private TableView<User> usersTable;
    @FXML
    private TableColumn<User, String> usernameColumn;
    @FXML
    private TableColumn<User, String> roleColumn;

    @FXML
    private TextField newUsernameField;
    @FXML
    private PasswordField newPasswordField;
    @FXML
    private PasswordField confirmPasswordField;
    @FXML
    private Button createCashierButton;
    @FXML
    private Button deleteUserButton;

    @FXML
    private Label statusMessageLabel;

    private final UserFileRepository userFileRepository = new UserFileRepository();
    private final UserService userService = new UserService(userFileRepository);

    private User selectedUser;

    @FXML
    // runs on screen load: guard non-admins out, then load the table
    private void initialize() {
        if (!Session.isAdmin()) { // guard: block cashiers from this screen
            Alert alert = new Alert(Alert.AlertType.ERROR,
                    "Managing users is only available to ADMIN users.", ButtonType.OK);
            alert.setTitle("Access Denied");
            alert.setHeaderText(null);
            alert.showAndWait();
            handleBack();
            return;
        }

        setupTableColumns();
        setupUserSelectionListener();
        roleLabel.setText("Logged in as: " + Session.getCurrentUsername() + " (" + Session.getCurrentRole() + ")");
        refreshUsers();
    }

    // wire table columns to User fields
    private void setupTableColumns() {
        usernameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getUsername()));
        roleColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getRole().toString()));
    }

    // enable delete only for a selected non-admin row
    private void setupUserSelectionListener() {
        usersTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            selectedUser = newVal;
            deleteUserButton.setDisable(newVal == null || newVal.getRole() == Role.ADMIN);
        });
        deleteUserButton.setDisable(true);
    }

    @FXML
    private void handleRefresh() {
        refreshUsers();
    }

    // reload the table from the service
    private void refreshUsers() {
        List<User> users = userService.getAllUsers();
        usersTable.setItems(FXCollections.observableArrayList(users));
        selectedUser = null;
        deleteUserButton.setDisable(true);
    }

    @FXML
    // add cashier button: validate + create via UserService
    private void handleCreateCashier() {
        try {
            Cashier cashier = userService.createCashierAccount(
                    newUsernameField.getText(), newPasswordField.getText(), confirmPasswordField.getText());
            showSuccess("Cashier account \"" + cashier.getUsername() + "\" created successfully.");
            clearForm();
            refreshUsers();
        } catch (InvalidUserException | DuplicateUsernameException e) {
            showError(e.getMessage());
        }
    }

    @FXML
    // delete button: confirm, then remove via UserService
    private void handleDeleteUser() {
        if (selectedUser == null) {
            showError("Please select an account to delete.");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION,
                "Are you sure you want to delete the Cashier account \"" + selectedUser.getUsername() + "\"? "
                        + "This cannot be undone.",
                ButtonType.YES, ButtonType.NO);
        confirmAlert.setTitle("Confirm Delete Account");
        confirmAlert.setHeaderText(null);

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.YES) {
            try {
                String deletedUsername = selectedUser.getUsername();
                userService.deleteCashierAccount(deletedUsername);
                showSuccess("Cashier account \"" + deletedUsername + "\" deleted successfully.");
                refreshUsers();
            } catch (InvalidUserException e) {
                showError(e.getMessage());
            }
        }
    }

    private void clearForm() {
        newUsernameField.clear();
        newPasswordField.clear();
        confirmPasswordField.clear();
    }

    @FXML
    // nav: back to dashboard
    private void handleBack() {
        try {
            Main.switchScene("view/dashboard.fxml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    private void showSuccess(String message) {
        statusMessageLabel.setStyle("-fx-text-fill: #2e7d32;");
        statusMessageLabel.setText(message);
    }
}