package com.inventory.exception;

/**
 * Thrown when a category name fails validation (e.g. blank, or "+ Add New
 * Category" submitted as if it were a real name).
 */
public class InvalidCategoryException extends RuntimeException {
    public InvalidCategoryException(String message) {
        super(message);
    }
}
