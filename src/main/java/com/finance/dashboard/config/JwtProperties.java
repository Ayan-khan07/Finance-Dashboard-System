package com.finance.dashboard.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(
        /* Base64-encoded HMAC key (>= 256 bits once decoded). */
        @NotBlank String secret,
        @Positive long expirationMinutes) {
}
