package com.inventory.model;

import com.inventory.exception.InsufficientStockException;
import com.inventory.exception.InvalidCartOperationException;

import java.util.ArrayList;
import java.util.List;

// POS cart: holds CartItems, uses List (Java Collections)
public class Cart {

    private final List<CartItem> items = new ArrayList<>();

    public void addItem(Product product, int quantity) {
        if (product == null) {
            throw new InvalidCartOperationException("No product selected.");
        }
        if (quantity <= 0) {
            throw new InvalidCartOperationException("Quantity must be greater than zero.");
        }

        CartItem existingItem = findItemByProductId(product.getId()); // merge if already in cart

        if (existingItem != null) {
            int newQuantity = existingItem.getQuantity() + quantity;
            ensureStockIsAvailable(product, newQuantity);
            existingItem.setQuantity(newQuantity);
        } else {
            ensureStockIsAvailable(product, quantity);
            items.add(new CartItem(product, quantity));
        }
    }

    public void removeItem(String productId) {
        CartItem item = findItemByProductId(productId);
        if (item == null) {
            throw new InvalidCartOperationException("That product is not in the cart.");
        }
        items.remove(item);
    }

    public void updateQuantity(String productId, int newQuantity) {
        CartItem item = findItemByProductId(productId);
        if (item == null) {
            throw new InvalidCartOperationException("That product is not in the cart.");
        }
        if (newQuantity <= 0) {
            throw new InvalidCartOperationException("Quantity must be greater than zero.");
        }
        ensureStockIsAvailable(item.getProduct(), newQuantity);
        item.setQuantity(newQuantity);
    }

    public void clear() {
        items.clear();
    }

    public List<CartItem> getItems() {
        return new ArrayList<>(items);
    }

    public double getSubtotal() {
        double subtotal = 0;
        for (CartItem item : items) {
            subtotal += item.getSubtotal();
        }
        return subtotal;
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    // guard: block adding more than what's in stock
    private void ensureStockIsAvailable(Product product, int requestedQuantity) {
        if (requestedQuantity > product.getQuantity()) {
            throw new InsufficientStockException(
                    "Not enough stock for " + product.getName() +
                            ". Available: " + product.getQuantity() + ", requested: " + requestedQuantity);
        }
    }

    // linear search by product id
    private CartItem findItemByProductId(String productId) {
        for (CartItem item : items) {
            if (item.getProduct().getId().equalsIgnoreCase(productId)) {
                return item;
            }
        }
        return null;
    }
}
