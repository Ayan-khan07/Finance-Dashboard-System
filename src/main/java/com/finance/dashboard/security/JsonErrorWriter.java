package com.finance.dashboard.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finance.dashboard.exception.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

/**
 * Security filters run before Spring MVC, so @RestControllerAdvice cannot handle their errors.
 * This writes the same {@link ApiError} JSON shape by hand.
 */
final class JsonErrorWriter {

    private JsonErrorWriter() {
    }

    static void write(ObjectMapper objectMapper,
                      HttpServletRequest request,
                      HttpServletResponse response,
                      HttpStatus status,
                      String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        ApiError body = new ApiError(
                Instant.now(), status.value(), status.getReasonPhrase(), message, request.getRequestURI());
        objectMapper.writeValue(response.getWriter(), body);
    }
}
