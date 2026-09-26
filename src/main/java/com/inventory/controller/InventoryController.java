package com.inventory.controller;

import com.inventory.Main;
import com.inventory.exception.DuplicateProductException;
import com.inventory.exception.InsufficientStockException;
import com.inventory.exception.InvalidCategoryException;
import com.inventory.exception.InvalidProductException;
import com.inventory.exception.DuplicateCategoryException;
import com.inventory.exception.ProductNotFoundException;
import com.inventory.model.Category;
import com.inventory.model.Product;
import com.inventory.repository.CategoryFileRepository;
import com.inventory.repository.ProductFileRepository;
import com.inventory.service.CategoryService;
import com.inventory.service.InventoryService;
import com.inventory.service.ProductService;
import com.inventory.util.CategoryManagerDialog;
import com.inventory.util.Session;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.ToggleButton;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// UI controller for inventory.fxml: product CRUD + stock in/out
public class InventoryController {

    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> categoryFilterComboBox;
    @FXML
    private ToggleButton lowStockToggleButton;
    @FXML
    private Button manageCategoriesButton;

    @FXML
    private TableView<Product> productTable;
    @FXML
    private TableColumn<Product, String> idColumn;
    @FXML
    private TableColumn<Product, String> nameColumn;
    @FXML
    private TableColumn<Product, String> categoryColumn;
    @FXML
    private TableColumn<Product, String> priceColumn;
    @FXML
    private TableColumn<Product, Integer> quantityColumn;
    @FXML
    private TableColumn<Product, Integer> minStockColumn;
    @FXML
    private TableColumn<Product, String> statusColumn;

    @FXML
    private TextField idField;
    @FXML
    private TextField nameField;
    @FXML
    private ComboBox<String> categoryFormComboBox;
    @FXML
    private TextField priceField;
    @FXML
    private TextField quantityField;
    @FXML
    private TextField minStockField;
    @FXML
    private Button addButton;
    @FXML
    private Button updateButton;
    @FXML
    private Button deleteButton;

    @FXML
    private Label selectedProductLabel;
    @FXML
    private TextField stockAmountField;
    @FXML
    private Button stockInButton;
    @FXML
    private Button stockOutButton;

    @FXML
    private Label statusMessageLabel;

    private final ProductFileRepository productFileRepository = new ProductFileRepository();
    private final CategoryFileRepository categoryFileRepository = new CategoryFileRepository();
    private final ProductService productService = new ProductService(productFileRepository);
    private final InventoryService inventoryService = new InventoryService(productFileRepository);
    private final CategoryService categoryService = new CategoryService(categoryFileRepository, productFileRepository);

    private static final String ADD_NEW_CATEGORY_OPTION = "+ Add New Category";

    private List<Product> allProducts = new ArrayList<>();

    private Product selectedProduct;

    @FXML
    // runs on screen load: build table, filters, listeners, permissions
    private void initialize() {
        setupTableColumns();
        setupFilterControls();
        setupSelectionListener();
        applyPermissions();
        refreshData();
    }

    // wire table columns to Product fields
    private void setupTableColumns() {
        idColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getId()));
        nameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));
        categoryColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getCategory().toDisplayString()));
        priceColumn.setCellValueFactory(data ->
                new SimpleStringProperty(String.format("%.2f", data.getValue().getPrice())));
        quantityColumn.setCellValueFactory(data ->
                new SimpleIntegerProperty(data.getValue().getQuantity()).asObject());
        minStockColumn.setCellValueFactory(data ->
                new SimpleIntegerProperty(data.getValue().getMinimumStock()).asObject());
        statusColumn.setCellValueFactory(data ->
                new SimpleStringProperty(inventoryService.isLowStock(data.getValue()) ? "LOW STOCK" : "OK"));

        statusColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(status);
                    setStyle("LOW STOCK".equals(status)
                            ? "-fx-text-fill: #c62828; -fx-font-weight: bold;"
                            : "-fx-text-fill: #2e7d32;");
                }
            }
        });
    }

    // wire search box, category filter, and category combo box
    private void setupFilterControls() {
        refreshCategoryFilterItems();
        categoryFilterComboBox.setValue("All");
        categoryFilterComboBox.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());

        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());

        refreshCategoryFormItems();

        categoryFormComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (ADD_NEW_CATEGORY_OPTION.equals(newVal)) {
                promptForNewCategory(oldVal);
            }
        });
    }

    // reload the category filter dropdown from CategoryService
    private void refreshCategoryFilterItems() {
        String previousValue = categoryFilterComboBox.getValue();
        List<String> items = new ArrayList<>();
        items.add("All");
        items.addAll(categoryService.getAllCategoryNames());
        categoryFilterComboBox.setItems(FXCollections.observableArrayList(items));
        categoryFilterComboBox.setValue(items.contains(previousValue) ? previousValue : "All");
    }

    // reload the add/edit form's category dropdown
    private void refreshCategoryFormItems() {
        List<String> items = new ArrayList<>(categoryService.getAllCategoryNames());
        items.add(ADD_NEW_CATEGORY_OPTION);
        categoryFormComboBox.setItems(FXCollections.observableArrayList(items));
    }

    // "+ Add New Category" option: prompt, create, select it
    private void promptForNewCategory(String previousValue) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Add New Category");
        dialog.setHeaderText(null);
        dialog.setContentText("New category name:");

        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty()) {
            categoryFormComboBox.setValue(previousValue);
            return;
        }

        try {
            Category newCategory = categoryService.addCategory(result.get());
            refreshCategoryFormItems();
            refreshCategoryFilterItems();
            categoryFormComboBox.setValue(newCategory.getName());
            showSuccess("Category added: " + newCategory.getName());
        } catch (InvalidCategoryException | DuplicateCategoryException e) {
            showError(e.getMessage());
            categoryFormComboBox.setValue(previousValue);
        }
    }

    // fill the edit form when a table row is selected
    private void setupSelectionListener() {
        productTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            selectedProduct = newVal;
            if (newVal != null) {
                idField.setText(newVal.getId());
                idField.setDisable(true);
                nameField.setText(newVal.getName());
                categoryFormComboBox.setValue(newVal.getCategory().getName());
                priceField.setText(String.valueOf(newVal.getPrice()));
                quantityField.setText(String.valueOf(newVal.getQuantity()));
                minStockField.setText(String.valueOf(newVal.getMinimumStock()));
                selectedProductLabel.setText("Selected Product: " + newVal.getName());
            } else {
                idField.setDisable(!Session.isAdmin());
                selectedProductLabel.setText("Selected Product: (none)");
            }
            updateActionButtonsState();
        });
    }

    // lock admin-only controls for cashiers
    private void applyPermissions() {
        boolean admin = Session.isAdmin();
        addButton.setDisable(!admin);
        manageCategoriesButton.setDisable(!admin);
        nameField.setDisable(!admin);
        categoryFormComboBox.setDisable(!admin);
        priceField.setDisable(!admin);
        quantityField.setDisable(!admin);
        minStockField.setDisable(!admin);
        stockAmountField.setDisable(!admin);
        if (!admin) {
            idField.setDisable(true);
        }
        updateActionButtonsState();
    }

    // enable update/delete/stock buttons only for admin + a selection
    private void updateActionButtonsState() {
        boolean allowed = Session.isAdmin() && selectedProduct != null;
        updateButton.setDisable(!allowed);
        deleteButton.setDisable(!allowed);
        stockInButton.setDisable(!allowed);
        stockOutButton.setDisable(!allowed);
    }

    // reload products from the service, then reapply filters
    private void refreshData() {
        allProducts = productService.getAllProducts();
        applyFilters();
    }

    // combine search + category + low-stock filters into one list
    private void applyFilters() {
        String keyword = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
        String categoryChoice = categoryFilterComboBox.getValue();
        boolean lowStockOnly = lowStockToggleButton.isSelected();

        List<Product> filtered = new ArrayList<>();
        for (Product product : allProducts) {
            boolean matchesKeyword = keyword.isEmpty()
                    || product.getId().toLowerCase().contains(keyword)
                    || product.getName().toLowerCase().contains(keyword);

            boolean matchesCategory = categoryChoice == null || "All".equals(categoryChoice)
                    || product.getCategory().toDisplayString().equalsIgnoreCase(categoryChoice);

            boolean matchesLowStock = !lowStockOnly || inventoryService.isLowStock(product);

            if (matchesKeyword && matchesCategory && matchesLowStock) {
                filtered.add(product);
            }
        }
        productTable.setItems(FXCollections.observableArrayList(filtered));
    }

    @FXML
    // refresh button: reset form and reload everything
    private void handleRefresh() {
        handleClearForm();
        refreshCategoryFilterItems();
        refreshCategoryFormItems();
        refreshData();
        statusMessageLabel.setText("");
    }

    @FXML
    // low-stock toggle button
    private void handleLowStockToggle() {
        applyFilters();
    }

    @FXML
    // manage-categories button: opens CategoryManagerDialog
    private void handleManageCategories() {
        if (!Session.isAdmin()) {
            return;
        }
        CategoryManagerDialog.showAndManage(categoryService);
        refreshCategoryFilterItems();
        refreshCategoryFormItems();
        refreshData();
    }

    @FXML
    // add button: build product from form, validate, persist
    private void handleAddProduct() {
        if (!Session.isAdmin()) {
            return;
        }
        try {
            Product product = buildProductFromForm();
            productService.addProduct(product);
            showSuccess("Product added: " + product.getName());
            handleClearForm();
            refreshData();
        } catch (NumberFormatException e) {
            showError("Price, Quantity, and Minimum Stock must be valid numbers.");
        } catch (InvalidProductException | DuplicateProductException e) {
            showError(e.getMessage());
        }
    }

    @FXML
    // update button: build product from form, validate, persist
    private void handleUpdateProduct() {
        if (!Session.isAdmin()) {
            return;
        }
        if (selectedProduct == null) {
            showError("Please select a product from the table to edit.");
            return;
        }
        try {
            Product product = buildProductFromForm();
            productService.updateProduct(product);
            showSuccess("Product updated: " + product.getName());
            handleClearForm();
            refreshData();
        } catch (NumberFormatException e) {
            showError("Price, Quantity, and Minimum Stock must be valid numbers.");
        } catch (InvalidProductException | ProductNotFoundException e) {
            showError(e.getMessage());
        }
    }

    @FXML
    // delete button: confirm, then remove the selected product
    private void handleDeleteProduct() {
        if (!Session.isAdmin()) {
            return;
        }
        if (selectedProduct == null) {
            showError("Please select a product from the table to delete.");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION,
                "Are you sure you want to delete " + selectedProduct.getName() + "?",
                ButtonType.YES, ButtonType.NO);
        confirmAlert.setTitle("Confirm Delete");
        confirmAlert.setHeaderText(null);

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.YES) {
            try {
                productService.deleteProduct(selectedProduct.getId());
                showSuccess("Product deleted.");
                handleClearForm();
                refreshData();
            } catch (ProductNotFoundException e) {
                showError(e.getMessage());
            }
        }
    }

    @FXML
    // clear the add/edit form and selection
    private void handleClearForm() {
        productTable.getSelectionModel().clearSelection();
        selectedProduct = null;
        idField.clear();
        idField.setDisable(!Session.isAdmin());
        nameField.clear();
        categoryFormComboBox.setValue(null);
        priceField.clear();
        quantityField.clear();
        minStockField.clear();
        selectedProductLabel.setText("Selected Product: (none)");
        updateActionButtonsState();
    }

    @FXML
    // stock-in button
    private void handleStockIn() {
        adjustStock(true);
    }

    @FXML
    // stock-out button
    private void handleStockOut() {
        adjustStock(false);
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

    // read + validate form fields into a new Product object
    private Product buildProductFromForm() {
        String id = idField.getText() == null ? "" : idField.getText().trim();
        String name = nameField.getText() == null ? "" : nameField.getText().trim();
        String categoryName = categoryFormComboBox.getValue();

        if (categoryName == null || categoryName.isBlank() || ADD_NEW_CATEGORY_OPTION.equals(categoryName)) {
            throw new InvalidProductException("Please select (or add) a category.");
        }
        Category category = Category.fromString(categoryName);

        double price = Double.parseDouble(priceField.getText().trim());
        int quantity = Integer.parseInt(quantityField.getText().trim());
        int minimumStock = Integer.parseInt(minStockField.getText().trim());

        return new Product(id, name, category, price, quantity, minimumStock);
    }

    // shared logic for the stock-in/stock-out buttons
    private void adjustStock(boolean isStockIn) {
        if (!Session.isAdmin()) {
            return;
        }
        if (selectedProduct == null) {
            showError("Please select a product first.");
            return;
        }

        try {
            int amount = Integer.parseInt(stockAmountField.getText().trim());
            String productId = selectedProduct.getId();

            if (isStockIn) {
                inventoryService.stockIn(productId, amount);
                showSuccess("Stocked in " + amount + " unit(s) of " + selectedProduct.getName());
            } else {
                inventoryService.stockOut(productId, amount);
                showSuccess("Stocked out " + amount + " unit(s) of " + selectedProduct.getName());
            }

            stockAmountField.clear();
            refreshData();
            reselectProductById(productId);
        } catch (NumberFormatException e) {
            showError("Amount must be a whole number.");
        } catch (InvalidProductException | InsufficientStockException | ProductNotFoundException e) {
            showError(e.getMessage());
        }
    }

    // re-highlight the product row after a refresh
    private void reselectProductById(String productId) {
        for (Product product : productTable.getItems()) {
            if (product.getId().equalsIgnoreCase(productId)) {
                productTable.getSelectionModel().select(product);
                break;
            }
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
