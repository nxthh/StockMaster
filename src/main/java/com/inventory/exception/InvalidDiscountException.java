package com.inventory.exception;

// custom exception (inheritance)
public class InvalidDiscountException extends RuntimeException {
    public InvalidDiscountException(String message) {
        super(message);
    }
}
