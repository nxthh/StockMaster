package com.inventory.service;

import com.inventory.model.Product;
import com.inventory.model.Transaction;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DashboardChartService {

    private static final DateTimeFormatter DAY_LABEL = DateTimeFormatter.ofPattern("MMM d");

    private final ProductService productService;
    private final TransactionService transactionService;

    public DashboardChartService(ProductService productService, TransactionService transactionService) {
        this.productService = productService;
        this.transactionService = transactionService;
    }

    public Map<String, Double> getRevenueForLastDays(int days) {
        LocalDate today = LocalDate.now();
        Map<LocalDate, Double> revenueByDate = new LinkedHashMap<>();
        for (int i = days - 1; i >= 0; i--) {
            revenueByDate.put(today.minusDays(i), 0.0);
        }

        for (Transaction transaction : transactionService.getVisibleTransactions()) {
            LocalDate date = transaction.getDateTime().toLocalDate();
            if (revenueByDate.containsKey(date)) {
                revenueByDate.put(date, revenueByDate.get(date) + transaction.getTotal());
            }
        }

        Map<String, Double> revenueByLabel = new LinkedHashMap<>();
        for (Map.Entry<LocalDate, Double> entry : revenueByDate.entrySet()) {
            revenueByLabel.put(entry.getKey().format(DAY_LABEL), entry.getValue());
        }
        return revenueByLabel;
    }

    public Map<String, Integer> getStockByCategory() {
        Map<String, Integer> stockByCategory = new LinkedHashMap<>();
        for (Product product : productService.getAllProducts()) {
            String categoryName = product.getCategory().toDisplayString();
            int current = stockByCategory.getOrDefault(categoryName, 0);
            stockByCategory.put(categoryName, current + product.getQuantity());
        }
        return stockByCategory;
    }

    public List<Product> getLowestStockProducts(int limit) {
        List<Product> sorted = new ArrayList<>(productService.getAllProducts());
        sorted.sort(Comparator.comparingInt(Product::getQuantity));
        if (sorted.size() > limit) {
            return sorted.subList(0, limit);
        }
        return sorted;
    }
}
