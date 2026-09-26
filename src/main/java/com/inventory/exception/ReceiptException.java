package com.inventory.exception;

// custom exception (inheritance)
public class ReceiptException extends RuntimeException {
    public ReceiptException(String message) {
        super(message);
    }

    public ReceiptException(String message, Throwable cause) {
        super(message, cause);
    }
}
