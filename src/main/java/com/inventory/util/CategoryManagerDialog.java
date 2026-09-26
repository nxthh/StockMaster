package com.inventory.util;

import com.inventory.exception.CategoryInUseException;
import com.inventory.exception.CategoryNotFoundException;
import com.inventory.exception.DuplicateCategoryException;
import com.inventory.exception.InvalidCategoryException;
import com.inventory.model.Category;
import com.inventory.service.CategoryService;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.Optional;

// popup dialog for add/rename/delete of categories
public class CategoryManagerDialog {

    private CategoryManagerDialog() {
    } // static-only: no instances

    public static void showAndManage(CategoryService categoryService) {
        TableView<Category> table = new TableView<>();
        table.setPrefSize(420, 260);
        table.setPlaceholder(new Label("No categories yet."));

        TableColumn<Category, String> nameColumn = new TableColumn<>("Category");
        nameColumn.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getName()));
        nameColumn.setPrefWidth(220);

        TableColumn<Category, String> usageColumn = new TableColumn<>("Products Using It");
        usageColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                String.valueOf(categoryService.countProductsUsingCategory(data.getValue().getName()))));
        usageColumn.setPrefWidth(140);
        usageColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String count, boolean empty) {
                super.updateItem(count, empty);
                setText(empty ? null : count);
                setStyle(empty ? "" : "-fx-alignment: CENTER;");
            }
        });

        table.getColumns().add(nameColumn);
        table.getColumns().add(usageColumn);

        TextField nameField = new TextField();
        nameField.setPromptText("Category name");
        nameField.setPrefWidth(200);

        Button addButton = new Button("Add");
        Button renameButton = new Button("Rename Selected");
        Button deleteButton = new Button("Delete Selected");
        renameButton.setDisable(true);
        deleteButton.setDisable(true);

        Label statusLabel = new Label();
        statusLabel.setWrapText(true);

        // reload the table from the service after every change
        Runnable refreshTable = () ->
                table.setItems(FXCollections.observableArrayList(categoryService.getAllCategories()));
        refreshTable.run();

        table.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            boolean selected = newVal != null;
            renameButton.setDisable(!selected);
            deleteButton.setDisable(!selected);
            if (selected) {
                nameField.setText(newVal.getName());
            }
        });

        addButton.setOnAction(e -> { // create category
            try {
                categoryService.addCategory(nameField.getText());
                nameField.clear();
                refreshTable.run();
                showStatus(statusLabel, "Category added.", false);
            } catch (InvalidCategoryException | DuplicateCategoryException ex) {
                showStatus(statusLabel, ex.getMessage(), true);
            }
        });

        renameButton.setOnAction(e -> { // rename selected category
            Category selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                return;
            }
            try {
                categoryService.renameCategory(selected.getName(), nameField.getText());
                refreshTable.run();
                showStatus(statusLabel, "Category renamed.", false);
            } catch (InvalidCategoryException | DuplicateCategoryException | CategoryNotFoundException ex) {
                showStatus(statusLabel, ex.getMessage(), true);
            }
        });

        deleteButton.setOnAction(e -> { // delete selected category, after confirm
            Category selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                return;
            }
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                    "Delete category \"" + selected.getName() + "\"?",
                    ButtonType.YES, ButtonType.NO);
            confirm.setTitle("Confirm Delete");
            confirm.setHeaderText(null);
            Optional<ButtonType> result = confirm.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.YES) {
                try {
                    categoryService.deleteCategory(selected.getName());
                    nameField.clear();
                    refreshTable.run();
                    showStatus(statusLabel, "Category deleted.", false);
                } catch (CategoryNotFoundException | CategoryInUseException ex) {
                    showStatus(statusLabel, ex.getMessage(), true);
                }
            }
        });

        HBox formRow = new HBox(8, nameField, addButton, renameButton, deleteButton);

        VBox content = new VBox(10,
                new Label("Every category currently in use by a product cannot be deleted."),
                table,
                formRow,
                statusLabel);
        content.setPadding(new Insets(10));

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Manage Categories");
        dialog.setHeaderText(null);
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }

    // shared success/error message styling for the status label
    private static void showStatus(Label label, String message, boolean isError) {
        label.setText(message);
        label.setStyle(isError ? "-fx-text-fill: #c62828;" : "-fx-text-fill: #2e7d32;");
    }
}
