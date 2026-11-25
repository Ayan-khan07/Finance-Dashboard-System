package com.finance.dashboard.exception;

/** The external AI provider failed, timed out or returned an unusable answer. */
public class AiServiceException extends RuntimeException {

    public AiServiceException(String message) {
        super(message);
    }

    public AiServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
