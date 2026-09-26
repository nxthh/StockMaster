package com.inventory.exception;

/**
 * Thrown when trying to add or rename a category to a name that already
 * belongs to another existing category (case-insensitive).
 */
public class DuplicateCategoryException extends RuntimeException {
    public DuplicateCategoryException(String message) {
        super(message);
    }
}
