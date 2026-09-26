package com.inventory.service;

import com.inventory.exception.DuplicateProductException;
import com.inventory.exception.InvalidProductException;
import com.inventory.exception.ProductNotFoundException;
import com.inventory.model.Category;
import com.inventory.model.Product;
import com.inventory.repository.ProductFileRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// business logic + validation for products (CRUD on top of the repository)
public class ProductService {

    private final ProductFileRepository productFileRepository;

    public ProductService(ProductFileRepository productFileRepository) {
        this.productFileRepository = productFileRepository;
    }

    public List<Product> getAllProducts() {
        return productFileRepository.loadAll();
    }

    // lookup by id, throws if not found
    public Product findById(String id) {
        Optional<Product> product = productFileRepository.findById(id);
        if (product.isEmpty()) {
            throw new ProductNotFoundException("No product found with ID: " + id);
        }
        return product.get();
    }

    public void addProduct(Product product) {
        validateProduct(product);

        if (productFileRepository.findById(product.getId()).isPresent()) { // guard: no duplicate IDs
            throw new DuplicateProductException("A product with ID " + product.getId() + " already exists.");
        }

        productFileRepository.add(product);
    }

    public void updateProduct(Product product) {
        validateProduct(product);

        findById(product.getId()); // throws if the product doesn't exist

        productFileRepository.update(product);
    }

    public void deleteProduct(String id) {
        findById(id); // throws if the product doesn't exist

        productFileRepository.delete(id);
    }

    // case-insensitive substring search on product name
    public List<Product> searchByName(String keyword) {
        List<Product> results = new ArrayList<>();
        String lowerKeyword = keyword.toLowerCase();

        for (Product product : getAllProducts()) {
            if (product.getName().toLowerCase().contains(lowerKeyword)) {
                results.add(product);
            }
        }
        return results;
    }

    // products belonging to one category
    public List<Product> filterByCategory(Category category) {
        List<Product> results = new ArrayList<>();

        for (Product product : getAllProducts()) {
            if (product.getCategory().equals(category)) {
                results.add(product);
            }
        }
        return results;
    }

    // field-by-field validation before create/update
    private void validateProduct(Product product) {
        if (product.getId() == null || product.getId().isBlank()) {
            throw new InvalidProductException("Product ID cannot be empty.");
        }
        if (product.getName() == null || product.getName().isBlank()) {
            throw new InvalidProductException("Product name cannot be empty.");
        }
        if (product.getPrice() < 0) {
            throw new InvalidProductException("Product price cannot be negative.");
        }
        if (product.getQuantity() < 0) {
            throw new InvalidProductException("Product quantity cannot be negative.");
        }
        if (product.getMinimumStock() < 0) {
            throw new InvalidProductException("Product minimum stock cannot be negative.");
        }
    }
}
