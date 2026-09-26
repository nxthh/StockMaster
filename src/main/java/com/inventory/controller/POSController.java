package com.inventory.controller;

import com.inventory.Main;
import com.inventory.exception.InsufficientStockException;
import com.inventory.exception.InvalidCartOperationException;
import com.inventory.exception.InvalidDiscountException;
import com.inventory.exception.PaymentException;
import com.inventory.model.Cart;
import com.inventory.model.CartItem;
import com.inventory.model.CheckoutTotals;
import com.inventory.model.Discount;
import com.inventory.model.PercentageDiscount;
import com.inventory.model.Product;
import com.inventory.model.Transaction;
import com.inventory.model.payment.CardPayment;
import com.inventory.model.payment.CashPayment;
import com.inventory.model.payment.Payment;
import com.inventory.model.payment.QRPayment;
import com.inventory.repository.CategoryFileRepository;
import com.inventory.repository.ProductFileRepository;
import com.inventory.repository.ReceiptFileRepository;
import com.inventory.repository.TransactionFileRepository;
import com.inventory.service.CategoryService;
import com.inventory.service.CheckoutService;
import com.inventory.service.InventoryService;
import com.inventory.service.ProductService;
import com.inventory.service.ReceiptService;
import com.inventory.service.TaxCalculator;
import com.inventory.exception.ReceiptException;
import com.inventory.util.ReceiptDialog;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// UI controller for pos.fxml: browse products, build cart, checkout
public class POSController {

    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> categoryFilterComboBox;
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
    private TableColumn<Product, Integer> stockColumn;

    @FXML
    private Label selectedProductLabel;
    @FXML
    private TextField quantityField;
    @FXML
    private Button addToCartButton;

    @FXML
    private TableView<CartItem> cartTable;
    @FXML
    private TableColumn<CartItem, String> cartProductColumn;
    @FXML
    private TableColumn<CartItem, Integer> cartQuantityColumn;
    @FXML
    private TableColumn<CartItem, String> cartPriceColumn;
    @FXML
    private TableColumn<CartItem, String> cartSubtotalColumn;

    @FXML
    private TextField updateQuantityField;
    @FXML
    private Button updateQuantityButton;
    @FXML
    private Button removeItemButton;
    @FXML
    private Button clearCartButton;

    @FXML
    private Label subtotalLabel;
    @FXML
    private Label statusMessageLabel;

    @FXML
    private TextField discountField;
    @FXML
    private Label discountAmountLabel;
    @FXML
    private Label taxAmountLabel;
    @FXML
    private Label totalLabel;
    @FXML
    private ComboBox<String> paymentMethodComboBox;
    @FXML
    private TextField amountPaidField;
    @FXML
    private Label changeLabel;
    @FXML
    private Button checkoutButton;

    private final ProductFileRepository productFileRepository = new ProductFileRepository();
    private final CategoryFileRepository categoryFileRepository = new CategoryFileRepository();
    private final ProductService productService = new ProductService(productFileRepository);
    private final InventoryService inventoryService = new InventoryService(productFileRepository);
    private final CategoryService categoryService = new CategoryService(categoryFileRepository, productFileRepository);

    private final TransactionFileRepository transactionFileRepository = new TransactionFileRepository();

    private final ReceiptFileRepository receiptFileRepository = new ReceiptFileRepository();
    private final ReceiptService receiptService = new ReceiptService(receiptFileRepository);

    private final TaxCalculator taxCalculator = new TaxCalculator(0.10);

    private final CheckoutService checkoutService = new CheckoutService(
            productService, inventoryService, taxCalculator, transactionFileRepository, receiptService);

    private final Cart cart = new Cart();

    private List<Product> allProducts = new ArrayList<>();

    private Product selectedProduct;

    private CartItem selectedCartItem;

    @FXML
    // runs on screen load: wire every table, listener, and combo box
    private void initialize() {
        setupProductTableColumns();
        setupCartTableColumns();
        setupProductSelectionListener();
        setupCartSelectionListener();
        setupSearchListener();
        setupCategoryFilterComboBox();
        setupPaymentMethodComboBox();
        setupCheckoutListeners();
        refreshProducts();
        refreshCartView();
        updateActionButtonsState();
    }

    // wire product table columns
    private void setupProductTableColumns() {
        idColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getId()));
        nameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));
        categoryColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getCategory().toDisplayString()));
        priceColumn.setCellValueFactory(data ->
                new SimpleStringProperty(String.format("%.2f", data.getValue().getPrice())));
        stockColumn.setCellValueFactory(data ->
                new SimpleIntegerProperty(data.getValue().getQuantity()).asObject());
    }

    // wire cart table columns
    private void setupCartTableColumns() {
        cartProductColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getProduct().getName()));
        cartQuantityColumn.setCellValueFactory(data ->
                new SimpleIntegerProperty(data.getValue().getQuantity()).asObject());
        cartPriceColumn.setCellValueFactory(data ->
                new SimpleStringProperty(String.format("%.2f", data.getValue().getProduct().getPrice())));
        cartSubtotalColumn.setCellValueFactory(data ->
                new SimpleStringProperty(String.format("%.2f", data.getValue().getSubtotal())));
    }

    // update label + default quantity when a product row is picked
    private void setupProductSelectionListener() {
        productTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            selectedProduct = newVal;
            if (newVal != null) {
                selectedProductLabel.setText(
                        "Selected: " + newVal.getName() + " (Stock: " + newVal.getQuantity() + ")");
                quantityField.setText("1");
            } else {
                selectedProductLabel.setText("Selected: (none)");
                quantityField.clear();
            }
            updateActionButtonsState();
        });
    }

    // fill the update-quantity field when a cart row is picked
    private void setupCartSelectionListener() {
        cartTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            selectedCartItem = newVal;
            updateQuantityField.setText(newVal == null ? "" : String.valueOf(newVal.getQuantity()));
            updateActionButtonsState();
        });
    }

    // re-filter products as the search box is typed in
    private void setupSearchListener() {
        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
    }

    // wire the category filter dropdown
    private void setupCategoryFilterComboBox() {
        populateCategoryFilterItems();
        categoryFilterComboBox.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
    }

    // reload category filter options from CategoryService
    private void populateCategoryFilterItems() {
        String previousValue = categoryFilterComboBox.getValue();
        List<String> items = new ArrayList<>();
        items.add("All");
        items.addAll(categoryService.getAllCategoryNames());
        categoryFilterComboBox.setItems(FXCollections.observableArrayList(items));
        categoryFilterComboBox.setValue(items.contains(previousValue) ? previousValue : "All");
    }

    // Cash/Card/QR selector; only Cash needs an amount-paid field
    private void setupPaymentMethodComboBox() {
        paymentMethodComboBox.setItems(FXCollections.observableArrayList("Cash", "Card", "QR"));
        paymentMethodComboBox.getSelectionModel().select("Cash");

        paymentMethodComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            boolean isCash = "Cash".equals(newVal);
            amountPaidField.setDisable(!isCash);
            if (!isCash) {
                amountPaidField.clear();
            }
            recalculateTotals();
        });
    }

    // recalc totals whenever discount or amount paid changes
    private void setupCheckoutListeners() {
        discountField.textProperty().addListener((obs, oldVal, newVal) -> recalculateTotals());
        amountPaidField.textProperty().addListener((obs, oldVal, newVal) -> recalculateTotals());
    }

    // enable add/update/remove buttons based on current selections
    private void updateActionButtonsState() {
        addToCartButton.setDisable(selectedProduct == null);
        updateQuantityButton.setDisable(selectedCartItem == null);
        removeItemButton.setDisable(selectedCartItem == null);
    }

    // reload products from the service, then reapply filters
    private void refreshProducts() {
        allProducts = productService.getAllProducts();
        applyFilters();
    }

    // combine search + category filters into one product list
    private void applyFilters() {
        String keyword = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
        String categoryChoice = categoryFilterComboBox.getValue();

        List<Product> filtered = new ArrayList<>();
        for (Product product : allProducts) {
            boolean matchesKeyword = keyword.isEmpty()
                    || product.getId().toLowerCase().contains(keyword)
                    || product.getName().toLowerCase().contains(keyword);
            boolean matchesCategory = categoryChoice == null || "All".equals(categoryChoice)
                    || product.getCategory().toDisplayString().equalsIgnoreCase(categoryChoice);
            if (matchesKeyword && matchesCategory) {
                filtered.add(product);
            }
        }
        productTable.setItems(FXCollections.observableArrayList(filtered));
    }

    // repaint the cart table, subtotal, and totals
    private void refreshCartView() {
        cartTable.setItems(FXCollections.observableArrayList(cart.getItems()));

        cartTable.refresh();

        subtotalLabel.setText("Subtotal: $" + String.format("%.2f", cart.getSubtotal()));
        checkoutButton.setDisable(cart.isEmpty());
        recalculateTotals();
    }

    // subtotal -> discount -> tax -> total -> change, live as fields change
    private void recalculateTotals() {
        double discountPercent;
        try {
            discountPercent = parseDiscountPercent();
        } catch (InvalidDiscountException e) { // invalid input, treat as no discount yet
            discountPercent = 0;
        }

        Discount discount = new PercentageDiscount(discountPercent);
        CheckoutTotals totals = checkoutService.calculateTotals(cart, discount);

        discountAmountLabel.setText("Discount: $" + String.format("%.2f", totals.getDiscountAmount()));
        taxAmountLabel.setText("Tax: $" + String.format("%.2f", totals.getTaxAmount()));
        totalLabel.setText("TOTAL: $" + String.format("%.2f", totals.getTotal()));

        if ("Cash".equals(paymentMethodComboBox.getValue())) { // change only applies to cash
            double amountPaid = parseAmountPaidLenient();
            double change = Math.max(amountPaid - totals.getTotal(), 0);
            changeLabel.setText("Change: $" + String.format("%.2f", change));
        } else {
            changeLabel.setText("Change: $0.00");
        }
    }

    @FXML
    // refresh button: reload product list and categories
    private void handleRefreshProducts() {
        productTable.getSelectionModel().clearSelection();
        populateCategoryFilterItems();
        refreshProducts();
        statusMessageLabel.setText("");
    }

    @FXML
    // add-to-cart button
    private void handleAddToCart() {
        if (selectedProduct == null) {
            showError("Please select a product first.");
            return;
        }
        try {
            int quantity = Integer.parseInt(quantityField.getText().trim());
            cart.addItem(selectedProduct, quantity);
            showSuccess("Added " + quantity + " x " + selectedProduct.getName() + " to cart.");
            refreshCartView();
        } catch (NumberFormatException e) {
            showError("Quantity must be a whole number.");
        } catch (InvalidCartOperationException | InsufficientStockException e) {
            showError(e.getMessage());
        }
    }

    @FXML
    // update-quantity button for the selected cart item
    private void handleUpdateQuantity() {
        if (selectedCartItem == null) {
            showError("Please select an item in the cart first.");
            return;
        }
        String productId = selectedCartItem.getProduct().getId();
        try {
            int newQuantity = Integer.parseInt(updateQuantityField.getText().trim());
            cart.updateQuantity(productId, newQuantity);
            showSuccess("Quantity updated.");
            refreshCartView();
            reselectCartItemById(productId);
        } catch (NumberFormatException e) {
            showError("Quantity must be a whole number.");
        } catch (InvalidCartOperationException | InsufficientStockException e) {
            showError(e.getMessage());
        }
    }

    @FXML
    // remove-item button for the selected cart item
    private void handleRemoveItem() {
        if (selectedCartItem == null) {
            showError("Please select an item in the cart first.");
            return;
        }
        try {
            cart.removeItem(selectedCartItem.getProduct().getId());
            showSuccess("Item removed from cart.");
            selectedCartItem = null;
            updateQuantityField.clear();
            refreshCartView();
        } catch (InvalidCartOperationException e) {
            showError(e.getMessage());
        }
    }

    @FXML
    // clear-cart button, with a confirmation prompt
    private void handleClearCart() {
        if (cart.isEmpty()) {
            showError("The cart is already empty.");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION,
                "Are you sure you want to clear the entire cart?", ButtonType.YES, ButtonType.NO);
        confirmAlert.setTitle("Confirm Clear Cart");
        confirmAlert.setHeaderText(null);

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.YES) {
            cart.clear();
            selectedCartItem = null;
            updateQuantityField.clear();
            refreshCartView();
            showSuccess("Cart cleared.");
        }
    }

    @FXML
    // checkout button: build payment, run CheckoutService, show receipt
    private void handleCheckout() {
        try {
            if (cart.isEmpty()) { // guard: nothing to sell
                showError("The cart is empty. Add a product before checking out.");
                return;
            }

            double discountPercent = parseDiscountPercent();
            Discount discount = new PercentageDiscount(discountPercent);

            CheckoutTotals totals = checkoutService.calculateTotals(cart, discount);

            String method = paymentMethodComboBox.getValue();
            Payment payment = buildPayment(method, totals.getTotal());

            Transaction transaction = checkoutService.checkout(cart, discount, payment);

            showCheckoutSuccess(transaction);
            showReceipt(transaction);
            resetCheckoutForm();
            refreshProducts();
            refreshCartView();

        } catch (InvalidCartOperationException | InsufficientStockException
                 | InvalidDiscountException | PaymentException | ReceiptException e) {
            showError(e.getMessage());
        }
    }

    // load and display the receipt that was just saved
    private void showReceipt(Transaction transaction) {
        try {
            String receiptText = receiptService.loadReceiptText(transaction.getReceiptId());
            ReceiptDialog.show("Receipt " + transaction.getReceiptId(), receiptText);
        } catch (ReceiptException e) {
            showError("The sale was completed, but the receipt could not be displayed: " + e.getMessage());
        }
    }

    // build the right Payment subclass for the chosen method (polymorphism)
    private Payment buildPayment(String method, double total) {
        if ("Cash".equals(method)) {
            double amountPaid = parseAmountPaid();
            return new CashPayment(total, amountPaid);
        } else if ("Card".equals(method)) {
            return new CardPayment(total);
        } else {
            return new QRPayment(total);
        }
    }

    // validate the discount field, 0-100 only
    private double parseDiscountPercent() {
        String text = discountField.getText() == null ? "" : discountField.getText().trim();
        if (text.isEmpty()) {
            return 0;
        }
        double value;
        try {
            value = Double.parseDouble(text);
        } catch (NumberFormatException e) {
            throw new InvalidDiscountException("Discount must be a number.");
        }
        if (value < 0 || value > 100) {
            throw new InvalidDiscountException("Discount must be between 0 and 100.");
        }
        return value;
    }

    // validate the amount-paid field for cash payments
    private double parseAmountPaid() {
        String text = amountPaidField.getText() == null ? "" : amountPaidField.getText().trim();
        if (text.isEmpty()) {
            throw new PaymentException("Please enter the amount paid.");
        }
        double amount;
        try {
            amount = Double.parseDouble(text);
        } catch (NumberFormatException e) {
            throw new PaymentException("Amount paid must be a valid number.");
        }
        if (amount < 0) {
            throw new PaymentException("Amount paid cannot be negative.");
        }
        return amount;
    }

    // same as parseAmountPaid, but 0 instead of throwing (for live totals)
    private double parseAmountPaidLenient() {
        try {
            return parseAmountPaid();
        } catch (PaymentException e) {
            return 0;
        }
    }

    // pop up a summary of the completed sale
    private void showCheckoutSuccess(Transaction transaction) {
        StringBuilder message = new StringBuilder();
        message.append(String.format("Transaction ID: %s%n", transaction.getTransactionId()));
        message.append(String.format("Subtotal: $%.2f%n", transaction.getSubtotal()));
        message.append(String.format("Discount: $%.2f%n", transaction.getDiscountAmount()));
        message.append(String.format("Tax: $%.2f%n", transaction.getTaxAmount()));
        message.append(String.format("Total: $%.2f%n", transaction.getTotal()));
        message.append(String.format("Payment Method: %s%n", transaction.getPaymentMethod()));
        if ("Cash".equals(transaction.getPaymentMethod())) {
            message.append(String.format("Amount Paid: $%.2f%n", transaction.getAmountPaid()));
        }
        message.append(String.format("Change: $%.2f", transaction.getChange()));

        Alert alert = new Alert(Alert.AlertType.INFORMATION, message.toString(), ButtonType.OK);
        alert.setTitle("Checkout Complete");
        alert.setHeaderText("Sale completed successfully");
        alert.showAndWait();
    }

    // reset discount/payment fields after a successful sale
    private void resetCheckoutForm() {
        discountField.setText("0");
        paymentMethodComboBox.getSelectionModel().select("Cash");
        amountPaidField.setDisable(false);
        amountPaidField.clear();
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

    // re-highlight the cart row after a refresh
    private void reselectCartItemById(String productId) {
        for (CartItem item : cartTable.getItems()) {
            if (item.getProduct().getId().equalsIgnoreCase(productId)) {
                cartTable.getSelectionModel().select(item);
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