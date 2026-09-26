package com.inventory.exception;

// custom exception (inheritance)
public class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException(String message) {
        super(message);
    }
}
