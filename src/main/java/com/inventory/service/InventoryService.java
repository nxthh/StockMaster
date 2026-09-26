package com.inventory.service;

import com.inventory.exception.InsufficientStockException;
import com.inventory.exception.InvalidProductException;
import com.inventory.exception.ProductNotFoundException;
import com.inventory.model.Product;
import com.inventory.repository.ProductFileRepository;

import java.util.ArrayList;
import java.util.List;

// business logic for stock levels (in/out), separate from ProductService
public class InventoryService {

    private final ProductFileRepository productFileRepository;

    public InventoryService(ProductFileRepository productFileRepository) {
        this.productFileRepository = productFileRepository;
    }

    // increase quantity, e.g. restocking
    public void stockIn(String productId, int amount) {
        if (amount <= 0) {
            throw new InvalidProductException("Stock-in amount must be greater than zero.");
        }

        Product product = findProductOrThrow(productId);
        product.setQuantity(product.getQuantity() + amount);
        productFileRepository.update(product);
    }

    // decrease quantity, e.g. a sale; guards against overselling
    public void stockOut(String productId, int amount) {
        if (amount <= 0) {
            throw new InvalidProductException("Stock-out amount must be greater than zero.");
        }

        Product product = findProductOrThrow(productId);

        if (amount > product.getQuantity()) {
            throw new InsufficientStockException(
                    "Not enough stock for " + product.getName() +
                            ". Available: " + product.getQuantity() + ", requested: " + amount);
        }

        product.setQuantity(product.getQuantity() - amount);
        productFileRepository.update(product);
    }

    public boolean isLowStock(Product product) {
        return product.getQuantity() <= product.getMinimumStock();
    }

    // products at or below their minimum stock level
    public List<Product> getLowStockProducts() {
        List<Product> lowStockProducts = new ArrayList<>();

        for (Product product : productFileRepository.loadAll()) {
            if (isLowStock(product)) {
                lowStockProducts.add(product);
            }
        }
        return lowStockProducts;
    }

    private Product findProductOrThrow(String productId) {
        return productFileRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(
                        "No product found with ID: " + productId));
    }
}
