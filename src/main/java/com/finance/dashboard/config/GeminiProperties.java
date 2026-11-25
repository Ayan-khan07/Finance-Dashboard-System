package com.finance.dashboard.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.gemini")
public record GeminiProperties(
        @NotBlank String apiKey,
        @NotBlank String baseUrl,
        @NotBlank String model,
        @Positive int connectTimeoutMs,
        @Positive int readTimeoutMs) {

    /** Keep the API key out of logs / actuator output. */
    @Override
    public String toString() {
        return "GeminiProperties[baseUrl=" + baseUrl + ", model=" + model + ", apiKey=***]";
    }
}
