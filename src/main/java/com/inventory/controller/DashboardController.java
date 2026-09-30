package com.inventory.controller;

import com.inventory.Main;
import com.inventory.model.Product;
import com.inventory.repository.ProductFileRepository;
import com.inventory.repository.TransactionFileRepository;
import com.inventory.service.DashboardChartService;
import com.inventory.service.InventoryService;
import com.inventory.service.ProductService;
import com.inventory.service.ReportService;
import com.inventory.service.TransactionService;
import com.inventory.util.Session;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import java.io.IOException;
import java.util.List;
import java.util.Map;

// UI controller for dashboard.fxml: summary stats + navigation hub
public class DashboardController {

    @FXML
    private Button inventoryButton;
    @FXML
    private Button reportsButton;
    @FXML
    private Button usersButton;

    @FXML
    private Label currentUserLabel;

    @FXML
    private Label totalProductsLabel;
    @FXML
    private Label totalInventoryItemsLabel;
    @FXML
    private Label lowStockProductsLabel;
    @FXML
    private Label totalTransactionsLabel;
    @FXML
    private Label totalRevenueLabel;

    @FXML
    private BarChart<String, Number> revenueBarChart;
    @FXML
    private PieChart categoryPieChart;
    @FXML
    private BarChart<String, Number> lowStockBarChart;

    private final ProductFileRepository productFileRepository = new ProductFileRepository();
    private final ProductService productService = new ProductService(productFileRepository);
    private final InventoryService inventoryService = new InventoryService(productFileRepository);

    private final TransactionFileRepository transactionFileRepository = new TransactionFileRepository();
    private final TransactionService transactionService = new TransactionService(transactionFileRepository);

    private final ReportService reportService =
            new ReportService(productService, inventoryService, transactionService);

    private final DashboardChartService chartService =
            new DashboardChartService(productService, transactionService);

    @FXML
    // runs on screen load: lock admin-only buttons, show summary
    private void initialize() {
        inventoryButton.setDisable(!Session.isAdmin());
        reportsButton.setDisable(!Session.isAdmin());
        usersButton.setDisable(!Session.isAdmin());
        currentUserLabel.setText(Session.getCurrentUsername() + " (" + Session.getCurrentRole() + ")");
        refreshSummary();
        refreshCharts();
    }

    // pull latest stats from ReportService into the labels
    private void refreshSummary() {
        totalProductsLabel.setText(String.valueOf(reportService.getTotalProducts()));
        totalInventoryItemsLabel.setText(String.valueOf(reportService.getTotalInventoryQuantity()));
        lowStockProductsLabel.setText(String.valueOf(reportService.getLowStockCount()));

        int transactionCount = reportService.getTotalTransactionCount(reportService.getAllVisibleTransactions());
        double revenue = reportService.getTotalRevenue(reportService.getAllVisibleTransactions());
        totalTransactionsLabel.setText(String.valueOf(transactionCount));
        totalRevenueLabel.setText(String.format("$%.2f", revenue));
    }

    private void refreshCharts() {
        XYChart.Series<String, Number> revenueSeries = new XYChart.Series<>();
        for (Map.Entry<String, Double> entry : chartService.getRevenueForLastDays(7).entrySet()) {
            revenueSeries.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }
        revenueBarChart.getData().clear();
        revenueBarChart.getData().add(revenueSeries);

        categoryPieChart.getData().clear();
        for (Map.Entry<String, Integer> entry : chartService.getStockByCategory().entrySet()) {
            if (entry.getValue() > 0) {
                categoryPieChart.getData().add(
                        new PieChart.Data(entry.getKey() + " (" + entry.getValue() + ")", entry.getValue()));
            }
        }

        XYChart.Series<String, Number> lowStockSeries = new XYChart.Series<>();
        List<Product> lowestStock = chartService.getLowestStockProducts(5);
        for (Product product : lowestStock) {
            lowStockSeries.getData().add(new XYChart.Data<>(product.getName(), product.getQuantity()));
        }
        lowStockBarChart.getData().clear();
        lowStockBarChart.getData().add(lowStockSeries);
    }

    @FXML
    private void handleRefreshDashboard() {
        refreshSummary();
        refreshCharts();
    }

    @FXML
    // nav: Inventory screen
    private void handleOpenInventory() {
        try {
            Main.switchScene("view/inventory.fxml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    // nav: POS screen
    private void handleOpenPOS() {
        try {
            Main.switchScene("view/pos.fxml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    // nav: Transactions screen
    private void handleOpenTransactions() {
        try {
            Main.switchScene("view/transactions.fxml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    // nav: Reports screen
    private void handleOpenReports() {
        try {
            Main.switchScene("view/reports.fxml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    // nav: Users screen
    private void handleOpenUsers() {
        try {
            Main.switchScene("view/users.fxml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    // nav: back to login
    private void handleLogout() {
        try {
            Main.switchScene("view/login.fxml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
