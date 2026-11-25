package com.finance.dashboard.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.finance.dashboard.config.GeminiProperties;
import com.finance.dashboard.exception.AiServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Thin client for the Gemini "generateContent" REST endpoint.
 * All failure modes (timeouts, connection errors, 4xx/5xx, empty or blocked answers) are
 * converted to {@link AiServiceException} so callers have exactly one thing to handle.
 */
@Slf4j
@Component
public class GeminiClient {

    private static final double TEMPERATURE = 0.4;
    // Generous ceiling: "thinking" models spend part of this budget before emitting visible text.
    private static final int MAX_OUTPUT_TOKENS = 2048;

    private final RestClient restClient;
    private final GeminiProperties properties;

    public GeminiClient(@Qualifier("geminiRestClient") RestClient restClient, GeminiProperties properties) {
        this.restClient = restClient;
        this.properties = properties;
    }

    public String generate(String systemPrompt, String userPrompt) {
        GenerateRequest body = new GenerateRequest(
                new Content(null, List.of(new Part(systemPrompt))),
                List.of(new Content("user", List.of(new Part(userPrompt)))),
                new GenerationConfig(TEMPERATURE, MAX_OUTPUT_TOKENS));

        GenerateResponse response;
        try {
            response = restClient.post()
                    .uri("/models/{model}:generateContent", properties.model())
                    .body(body)
                    .retrieve()
                    .body(GenerateResponse.class);
        } catch (RestClientResponseException e) {
            // Log status only: the response body could echo request content.
            log.warn("Gemini API returned HTTP {}", e.getStatusCode().value());
            throw new AiServiceException("Gemini API returned HTTP " + e.getStatusCode().value(), e);
        } catch (RestClientException e) {
            // Covers connect/read timeouts (ResourceAccessException) and unreadable responses.
            log.warn("Gemini API call failed: {}", e.getClass().getSimpleName());
            throw new AiServiceException("Gemini API call failed: " + e.getClass().getSimpleName(), e);
        }

        String text = extractText(response);
        if (text == null || text.isBlank()) {
            throw new AiServiceException("Gemini API returned no text (empty or blocked response)");
        }
        return text.trim();
    }

    private static String extractText(GenerateResponse response) {
        if (response == null || response.candidates() == null || response.candidates().isEmpty()) {
            return null;
        }
        Content content = response.candidates().get(0).content();
        if (content == null || content.parts() == null) {
            return null;
        }
        return content.parts().stream()
                .map(Part::text)
                .filter(Objects::nonNull)
                .collect(Collectors.joining());
    }

    // ---------------------------------------------------------------- wire format

    @JsonInclude(JsonInclude.Include.NON_NULL)
    record GenerateRequest(Content systemInstruction, List<Content> contents, GenerationConfig generationConfig) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    record Content(String role, List<Part> parts) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    record Part(String text) {
    }

    record GenerationConfig(double temperature, int maxOutputTokens) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record GenerateResponse(List<Candidate> candidates) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Candidate(Content content, String finishReason) {
    }
}
