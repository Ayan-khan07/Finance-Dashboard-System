package com.finance.dashboard.exception;

import java.time.Instant;

/** Standard error payload returned by every failing endpoint (including 401/403 from the security layer). */
public record ApiError(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path) {
}
