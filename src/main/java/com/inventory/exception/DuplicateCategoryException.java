package com.inventory.exception;

// custom exception (inheritance)
public class DuplicateCategoryException extends RuntimeException {
    public DuplicateCategoryException(String message) {
        super(message);
    }
}
