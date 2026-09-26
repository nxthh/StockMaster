package com.inventory.exception;

/**
 * Thrown when trying to delete a category that is still assigned to one
 * or more existing products. Deleting it would leave those products
 * pointing at a category that no longer exists, so the delete is blocked
 * until the products are re-categorized or removed first.
 */
public class CategoryInUseException extends RuntimeException {
    public CategoryInUseException(String message) {
        super(message);
    }
}
