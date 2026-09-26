package com.inventory.controller;

import com.inventory.Main;
import com.inventory.exception.ReceiptException;
import com.inventory.exception.UnauthorizedActionException;
import com.inventory.model.CartItem;
import com.inventory.model.Transaction;
import com.inventory.repository.ReceiptFileRepository;
import com.inventory.repository.TransactionFileRepository;
import com.inventory.service.ReceiptService;
import com.inventory.service.TransactionService;
import com.inventory.util.ReceiptDialog;
import com.inventory.util.Session;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

// UI controller for transactions.fxml: history, details, receipt, delete
public class TransactionController {

    @FXML
    private Label roleLabel;

    @FXML
    private TableView<Transaction> transactionTable;
    @FXML
    private TableColumn<Transaction, String> idColumn;
    @FXML
    private TableColumn<Transaction, String> dateColumn;
    @FXML
    private TableColumn<Transaction, String> cashierColumn;
    @FXML
    private TableColumn<Transaction, String> totalColumn;
    @FXML
    private TableColumn<Transaction, String> paymentMethodColumn;

    @FXML
    private Button viewDetailsButton;
    @FXML
    private Button viewReceiptButton;
    @FXML
    private Button deleteTransactionButton;
    @FXML
    private Button clearHistoryButton;
    @FXML
    private Label statusLabel;

    private static final DateTimeFormatter DATE_TIME_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final TransactionFileRepository transactionFileRepository = new TransactionFileRepository();
    private final TransactionService transactionService = new TransactionService(transactionFileRepository);

    private final ReceiptFileRepository receiptFileRepository = new ReceiptFileRepository();
    private final ReceiptService receiptService = new ReceiptService(receiptFileRepository);

    private Transaction selectedTransaction;

    @FXML
    // runs on screen load: set up table + role-based button access
    private void initialize() {
        setupTableColumns();
        setupSelectionListener();
        roleLabel.setText("Logged in as: " + Session.getCurrentUsername() + " (" + Session.getCurrentRole() + ")");
        clearHistoryButton.setDisable(!Session.isAdmin());
        refreshTransactions();
        updateActionButtonsState();
    }

    // wire table columns to Transaction fields
    private void setupTableColumns() {
        idColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTransactionId()));
        dateColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getDateTime().format(DATE_TIME_FORMAT)));
        cashierColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCashier()));
        totalColumn.setCellValueFactory(data ->
                new SimpleStringProperty(String.format("$%.2f", data.getValue().getTotal())));
        paymentMethodColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPaymentMethod()));
    }

    // keep action buttons in sync with the selected row
    private void setupSelectionListener() {
        transactionTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            selectedTransaction = newVal;
            updateActionButtonsState();
        });
    }

    // enable/disable buttons based on selection + admin role
    private void updateActionButtonsState() {
        boolean hasSelection = selectedTransaction != null;
        viewDetailsButton.setDisable(!hasSelection);
        viewReceiptButton.setDisable(!hasSelection);

        deleteTransactionButton.setDisable(!hasSelection || !Session.isAdmin());
    }

    @FXML
    private void handleRefresh() {
        refreshTransactions();
    }

    // reload the table (admins see all, cashiers see their own)
    private void refreshTransactions() {
        List<Transaction> transactions = transactionService.getVisibleTransactions();
        transactionTable.setItems(FXCollections.observableArrayList(transactions));
        statusLabel.setText(transactions.isEmpty() ? "No transactions found." : "");
    }

    @FXML
    // view details button: build and show a line-item breakdown
    private void handleViewDetails() {
        if (selectedTransaction == null) {
            showError("Please select a transaction first.");
            return;
        }

        StringBuilder message = new StringBuilder();
        message.append(String.format("Date: %s%n", selectedTransaction.getDateTime().format(DATE_TIME_FORMAT)));
        message.append(String.format("Cashier: %s%n%n", selectedTransaction.getCashier()));

        for (CartItem item : selectedTransaction.getItems()) { // one line per item
            message.append(String.format("%-20s x%-4d $%.2f%n",
                    item.getProduct().getName(), item.getQuantity(), item.getSubtotal()));
        }
        message.append(System.lineSeparator());

        message.append(String.format("Subtotal: $%.2f%n", selectedTransaction.getSubtotal()));
        message.append(String.format("Discount: $%.2f%n", selectedTransaction.getDiscountAmount()));
        message.append(String.format("Tax: $%.2f%n", selectedTransaction.getTaxAmount()));
        message.append(String.format("Total: $%.2f%n%n", selectedTransaction.getTotal()));

        message.append(String.format("Payment Method: %s%n", selectedTransaction.getPaymentMethod()));
        if ("Cash".equals(selectedTransaction.getPaymentMethod())) { // show paid/change only for cash
            message.append(String.format("Amount Paid: $%.2f%n", selectedTransaction.getAmountPaid()));
            message.append(String.format("Change: $%.2f", selectedTransaction.getChange()));
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION, message.toString(), ButtonType.OK);
        alert.setTitle("Transaction " + selectedTransaction.getTransactionId());
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    @FXML
    // view receipt button: load and display the saved receipt text
    private void handleViewReceipt() {
        if (selectedTransaction == null) {
            showError("Please select a transaction first.");
            return;
        }

        if (!selectedTransaction.hasReceipt()) {
            showError("No receipt was saved for this transaction.");
            return;
        }

        try {
            String receiptText = receiptService.loadReceiptText(selectedTransaction.getReceiptId());
            ReceiptDialog.show("Receipt " + selectedTransaction.getReceiptId(), receiptText);
        } catch (ReceiptException e) {
            showError("Could not open the saved receipt: " + e.getMessage());
        }
    }

    @FXML
    // delete button: confirm, remove transaction + its receipt
    private void handleDeleteTransaction() {
        if (selectedTransaction == null) {
            showError("Please select a transaction first.");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION,
                "Delete transaction " + selectedTransaction.getTransactionId()
                        + "? This cannot be undone.", ButtonType.YES, ButtonType.NO);
        confirmAlert.setTitle("Confirm Delete");
        confirmAlert.setHeaderText(null);

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.YES) {
            return;
        }

        try {
            if (selectedTransaction.hasReceipt()) {
                receiptFileRepository.deleteReceipt(selectedTransaction.getReceiptId());
            }
            transactionService.deleteTransaction(selectedTransaction.getTransactionId());
            selectedTransaction = null;
            refreshTransactions();
            updateActionButtonsState();
            statusLabel.setText("Transaction deleted.");
        } catch (UnauthorizedActionException e) {
            showError(e.getMessage());
        }
    }

    @FXML
    // clear-all button: admin-only, wipes every transaction
    private void handleClearHistory() {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION,
                "Delete ALL transaction history? This cannot be undone.", ButtonType.YES, ButtonType.NO);
        confirmAlert.setTitle("Confirm Clear All History");
        confirmAlert.setHeaderText(null);

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.YES) {
            return;
        }

        try {
            transactionService.clearAllHistory();
            selectedTransaction = null;
            refreshTransactions();
            updateActionButtonsState();
            statusLabel.setText("All transaction history has been cleared.");
        } catch (UnauthorizedActionException e) {
            showError(e.getMessage());
        }
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
}
