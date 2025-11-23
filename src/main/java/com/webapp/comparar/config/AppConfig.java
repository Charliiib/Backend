package com.webapp.comparar.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Value("${app.jwtSecret:defaultSecretKeyForDevelopmentOnlyChangeInProduction}")
    private String jwtSecret;

    @Value("${app.jwtExpirationInMs:86400000}")
    private int jwtExpirationMs;

    @Value("${google.ai.api.key:}")
    private String googleAiApiKey;

    // Getters
    public String getJwtSecret() {
        return jwtSecret;
    }

    public int getJwtExpirationMs() {
        return jwtExpirationMs;
    }

    public String getGoogleAiApiKey() {
        return googleAiApiKey;
    }
}