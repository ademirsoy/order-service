package com.ademirsoy.orderservice.exception;


public class DataAccessException extends RuntimeException {
    public DataAccessException(String message) {
        super(message);
    }
}
