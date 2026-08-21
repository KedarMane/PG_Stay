package com.pgms.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
public class RestClientConfig {

    // Short timeouts matter here: if payment-service is slow or down, a guest clicking
    // "Pay rent" should get a clear failure quickly rather than a hung request. See
    // PaymentServiceClient for how ResourceAccessException (timeout/connection refused) is
    // turned into a friendly message instead of a raw stack trace.
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .connectTimeout(Duration.ofSeconds(3))
                .readTimeout(Duration.ofSeconds(8))
                .build();
    }
}
