package com.finance.dashboard.exception;

/** Request conflicts with current state, e.g. duplicate username (HTTP 409). */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
