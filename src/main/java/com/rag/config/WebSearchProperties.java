package com.rag.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for optional SearXNG web search integration.
 *
 * @param enabled    whether web search tool calling is enabled
 * @param baseUrl    SearXNG instance base URL
 * @param maxResults maximum number of search results to include in tool output
 */
@ConfigurationProperties(prefix = "rag.web-search")
public record WebSearchProperties(
        boolean enabled,
        String baseUrl,
        int maxResults
) {
}
