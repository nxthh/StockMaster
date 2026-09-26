package com.inventory.exception;

// custom exception (inheritance)
public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(String message) {
        super(message);
    }
}
