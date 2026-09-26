package com.inventory.controller;

import com.inventory.Main;
import com.inventory.model.Product;
import com.inventory.model.Transaction;
import com.inventory.repository.ProductFileRepository;
import com.inventory.repository.TransactionFileRepository;
import com.inventory.service.InventoryService;
import com.inventory.service.ProductService;
import com.inventory.service.ReportService;
import com.inventory.service.TransactionService;
import com.inventory.util.Session;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

// UI controller for reports.fxml: admin-only inventory/sales/low-stock views
public class ReportsController {

    @FXML
    private Label totalInventoryValueLabel;
    @FXML
    private TableView<Product> inventoryTable;
    @FXML
    private TableColumn<Product, String> invProductColumn;
    @FXML
    private TableColumn<Product, String> invCategoryColumn;
    @FXML
    private TableColumn<Product, String> invPriceColumn;
    @FXML
    private TableColumn<Product, Integer> invQuantityColumn;
    @FXML
    private TableColumn<Product, String> invValueColumn;

    @FXML
    private DatePicker fromDatePicker;
    @FXML
    private DatePicker toDatePicker;
    @FXML
    private Label totalTransactionsLabel;
    @FXML
    private Label totalRevenueLabel;
    @FXML
    private Label totalDiscountLabel;
    @FXML
    private Label totalTaxLabel;
    @FXML
    private Label salesStatusLabel;

    @FXML
    private TableView<Product> lowStockTable;
    @FXML
    private TableColumn<Product, String> lowStockProductColumn;
    @FXML
    private TableColumn<Product, Integer> lowStockQuantityColumn;
    @FXML
    private TableColumn<Product, Integer> lowStockMinColumn;
    @FXML
    private TableColumn<Product, String> lowStockStatusColumn;

    private final ProductFileRepository productFileRepository = new ProductFileRepository();
    private final ProductService productService = new ProductService(productFileRepository);
    private final InventoryService inventoryService = new InventoryService(productFileRepository);

    private final TransactionFileRepository transactionFileRepository = new TransactionFileRepository();
    private final TransactionService transactionService = new TransactionService(transactionFileRepository);

    private final ReportService reportService =
            new ReportService(productService, inventoryService, transactionService);

    private List<Transaction> allTransactions;

    @FXML
    // runs on screen load: guard non-admins out, load all three reports
    private void initialize() {
        if (!Session.isAdmin()) { // guard: block cashiers from this screen
            Alert alert = new Alert(Alert.AlertType.ERROR,
                    "Reports are only available to ADMIN users.", ButtonType.OK);
            alert.setTitle("Access Denied");
            alert.setHeaderText(null);
            alert.showAndWait();
            handleBack();
            return;
        }

        setupInventoryTable();
        setupLowStockTable();

        refreshInventoryReport();
        refreshLowStockReport();

        allTransactions = reportService.getAllVisibleTransactions();
        showSalesReport(allTransactions);
    }

    // wire inventory-value table columns
    private void setupInventoryTable() {
        invProductColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));
        invCategoryColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getCategory().toDisplayString()));
        invPriceColumn.setCellValueFactory(data ->
                new SimpleStringProperty(String.format("$%.2f", data.getValue().getPrice())));
        invQuantityColumn.setCellValueFactory(data ->
                new SimpleIntegerProperty(data.getValue().getQuantity()).asObject());
        invValueColumn.setCellValueFactory(data ->
                new SimpleStringProperty(String.format("$%.2f", reportService.getInventoryValue(data.getValue()))));
    }

    @FXML
    private void handleRefreshInventoryReport() {
        refreshInventoryReport();
    }

    // reload inventory table + total value label
    private void refreshInventoryReport() {
        List<Product> products = productService.getAllProducts();
        inventoryTable.setItems(FXCollections.observableArrayList(products));
        totalInventoryValueLabel.setText(String.format("Total Inventory Value: $%.2f",
                reportService.getTotalInventoryValue()));
    }

    @FXML
    // apply filter button: validate range, then re-show sales report
    private void handleApplyDateFilter() {
        LocalDate from = fromDatePicker.getValue();
        LocalDate to = toDatePicker.getValue();

        if (from != null && to != null && from.isAfter(to)) { // guard: invalid range
            salesStatusLabel.setText("\"From\" date must not be after \"To\" date.");
            return;
        }

        List<Transaction> filtered = reportService.filterByDateRange(allTransactions, from, to);
        showSalesReport(filtered);
    }

    @FXML
    // clear filter button: reset pickers, show unfiltered sales
    private void handleClearDateFilter() {
        fromDatePicker.setValue(null);
        toDatePicker.setValue(null);
        showSalesReport(allTransactions);
    }

    // paint the sales summary labels for the given transaction list
    private void showSalesReport(List<Transaction> transactions) {
        totalTransactionsLabel.setText(
                "Total Transactions: " + reportService.getTotalTransactionCount(transactions));
        totalRevenueLabel.setText(
                String.format("Total Revenue: $%.2f", reportService.getTotalRevenue(transactions)));
        totalDiscountLabel.setText(
                String.format("Total Discount: $%.2f", reportService.getTotalDiscount(transactions)));
        totalTaxLabel.setText(
                String.format("Total Tax: $%.2f", reportService.getTotalTax(transactions)));
        salesStatusLabel.setText(transactions.isEmpty() ? "No transactions found for this range." : "");
    }

    // wire low-stock table columns (status column always reads LOW STOCK)
    private void setupLowStockTable() {
        lowStockProductColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));
        lowStockQuantityColumn.setCellValueFactory(data ->
                new SimpleIntegerProperty(data.getValue().getQuantity()).asObject());
        lowStockMinColumn.setCellValueFactory(data ->
                new SimpleIntegerProperty(data.getValue().getMinimumStock()).asObject());
        lowStockStatusColumn.setCellValueFactory(data -> new SimpleStringProperty("LOW STOCK"));

        lowStockStatusColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(status);
                    setStyle("-fx-text-fill: #c62828; -fx-font-weight: bold;");
                }
            }
        });
    }

    @FXML
    private void handleRefreshLowStockReport() {
        refreshLowStockReport();
    }

    // reload the low-stock table
    private void refreshLowStockReport() {
        List<Product> lowStockProducts = reportService.getLowStockProducts();
        lowStockTable.setItems(FXCollections.observableArrayList(lowStockProducts));
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
}
