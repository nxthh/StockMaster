package com.inventory.exception;

// custom exception (inheritance)
public class InvalidCategoryException extends RuntimeException {
    public InvalidCategoryException(String message) {
        super(message);
    }
}
