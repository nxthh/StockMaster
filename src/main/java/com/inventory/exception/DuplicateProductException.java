package com.inventory.exception;

// custom exception (inheritance)
public class DuplicateProductException extends RuntimeException {
    public DuplicateProductException(String message) {
        super(message);
    }
}
