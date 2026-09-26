package com.inventory.exception;

/**
 * Thrown when code tries to rename or delete a category that does not
 * exist in data/categories.txt.
 */
public class CategoryNotFoundException extends RuntimeException {
    public CategoryNotFoundException(String message) {
        super(message);
    }
}
