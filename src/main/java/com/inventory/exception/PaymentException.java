package com.inventory.exception;

// custom exception (inheritance)
public class PaymentException extends RuntimeException {
    public PaymentException(String message) {
        super(message);
    }
}
