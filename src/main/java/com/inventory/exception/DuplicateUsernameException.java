package com.inventory.exception;

// custom exception (inheritance)
public class DuplicateUsernameException extends RuntimeException {
    public DuplicateUsernameException(String message) {
        super(message);
    }
}
