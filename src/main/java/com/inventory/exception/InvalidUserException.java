package com.inventory.exception;

// custom exception (inheritance)
public class InvalidUserException extends RuntimeException {
    public InvalidUserException(String message) {
        super(message);
    }
}
