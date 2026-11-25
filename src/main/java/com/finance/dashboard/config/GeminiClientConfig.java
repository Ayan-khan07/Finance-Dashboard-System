package com.finance.dashboard.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/**
 * RestClient dedicated to the Gemini API. Hard connect/read timeouts guarantee that a slow or
 * unreachable AI provider can never hang a request thread indefinitely.
 * The key is sent in a header (not the query string) so it never shows up in URLs or access logs.
 */
@Configuration
public class GeminiClientConfig {

    @Bean
    public RestClient geminiRestClient(RestClient.Builder builder, GeminiProperties props) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(props.connectTimeoutMs());
        requestFactory.setReadTimeout(props.readTimeoutMs());

        return builder
                .baseUrl(props.baseUrl())
                .requestFactory(requestFactory)
                .defaultHeader("x-goog-api-key", props.apiKey())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
}
