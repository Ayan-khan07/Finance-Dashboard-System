package com.finance.dashboard.exception;

/** Business-rule violation caused by the client (HTTP 400). */
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}
