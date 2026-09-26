package com.inventory.exception;

// custom exception (inheritance)
public class InvalidProductException extends RuntimeException {
    public InvalidProductException(String message) {
        super(message);
    }
}
