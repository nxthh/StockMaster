package com.inventory.exception;

// custom exception (inheritance)
public class InvalidCartOperationException extends RuntimeException {
    public InvalidCartOperationException(String message) {
        super(message);
    }
}
