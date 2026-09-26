package com.inventory.exception;

// custom exception (inheritance)
public class CategoryInUseException extends RuntimeException {
    public CategoryInUseException(String message) {
        super(message);
    }
}
