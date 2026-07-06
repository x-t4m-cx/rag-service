package com.rag.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * OpenAI-compatible API configuration.
 *
 * @param defaultModel default model id exposed via {@code /v1/models}
 * @param modelOwner   owner field returned in model listings
 */
@ConfigurationProperties(prefix = "openai.api")
public record OpenAiApiProperties(
        String defaultModel,
        String modelOwner
) {
}
