package com.inventory.exception;

// custom exception (inheritance)
public class InvalidLoginException extends RuntimeException {
    public InvalidLoginException(String message) {
        super(message);
    }
}
