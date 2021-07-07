package com.ademirsoy.orderservice.exception;


public class ConflictingOrderException extends RuntimeException {
    public ConflictingOrderException(String message) {
        super(message);
    }
}
