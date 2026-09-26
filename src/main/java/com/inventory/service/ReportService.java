package com.inventory.service;

import com.inventory.model.Product;
import com.inventory.model.Transaction;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// aggregates data from other services for the dashboard/reports screens
public class ReportService {

    private final ProductService productService;
    private final InventoryService inventoryService;
    private final TransactionService transactionService;

    public ReportService(ProductService productService,
                          InventoryService inventoryService,
                          TransactionService transactionService) {
        this.productService = productService;
        this.inventoryService = inventoryService;
        this.transactionService = transactionService;
    }

    public int getTotalProducts() {
        return productService.getAllProducts().size();
    }

    public int getTotalInventoryQuantity() {
        int total = 0;
        for (Product product : productService.getAllProducts()) {
            total += product.getQuantity();
        }
        return total;
    }

    public List<Product> getLowStockProducts() {
        return inventoryService.getLowStockProducts();
    }

    public int getLowStockCount() {
        return getLowStockProducts().size();
    }

    public double getInventoryValue(Product product) {
        return product.getPrice() * product.getQuantity();
    }

    public double getTotalInventoryValue() {
        double total = 0;
        for (Product product : productService.getAllProducts()) {
            total += getInventoryValue(product);
        }
        return total;
    }

    public List<Transaction> getAllVisibleTransactions() {
        return transactionService.getVisibleTransactions();
    }

    // keep transactions whose date falls within [startDate, endDate]
    public List<Transaction> filterByDateRange(List<Transaction> transactions,
                                                LocalDate startDate, LocalDate endDate) {
        List<Transaction> filtered = new ArrayList<>();
        for (Transaction transaction : transactions) {
            LocalDate saleDate = transaction.getDateTime().toLocalDate();
            boolean notBeforeStart = (startDate == null) || !saleDate.isBefore(startDate);
            boolean notAfterEnd = (endDate == null) || !saleDate.isAfter(endDate);
            if (notBeforeStart && notAfterEnd) {
                filtered.add(transaction);
            }
        }
        return filtered;
    }

    public int getTotalTransactionCount(List<Transaction> transactions) {
        return transactions.size();
    }

    public double getTotalRevenue(List<Transaction> transactions) {
        double total = 0;
        for (Transaction transaction : transactions) {
            total += transaction.getTotal();
        }
        return total;
    }

    public double getTotalDiscount(List<Transaction> transactions) {
        double total = 0;
        for (Transaction transaction : transactions) {
            total += transaction.getDiscountAmount();
        }
        return total;
    }

    public double getTotalTax(List<Transaction> transactions) {
        double total = 0;
        for (Transaction transaction : transactions) {
            total += transaction.getTaxAmount();
        }
        return total;
    }
}
