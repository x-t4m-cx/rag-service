package com.rag.websearch;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rag.config.WebSearchProperties;
import com.rag.exception.RagException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Calls a SearXNG instance and formats JSON search results for LLM consumption.
 */
@Service
@ConditionalOnProperty(name = "rag.web-search.enabled", havingValue = "true")
public class SearXngWebSearchService implements WebSearchService {

    private static final Logger log = LoggerFactory.getLogger(SearXngWebSearchService.class);
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(15);

    private final WebClient webClient;
    private final WebSearchProperties webSearchProperties;
    private final ObjectMapper objectMapper;

    /**
     * Creates the SearXNG web search service.
     *
     * @param webSearchProperties web search configuration
     * @param objectMapper        JSON mapper
     */
    public SearXngWebSearchService(WebSearchProperties webSearchProperties, ObjectMapper objectMapper) {
        this.webSearchProperties = webSearchProperties;
        this.objectMapper = objectMapper;
        this.webClient = WebClient.builder()
                .baseUrl(webSearchProperties.baseUrl())
                .build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String search(String query) {
        if (query == null || query.isBlank()) {
            return "Search query must not be blank.";
        }

        long start = System.currentTimeMillis();
        log.info("Executing web search query='{}'", query);

        String uri = UriComponentsBuilder.fromPath("/search")
                .queryParam("q", query.trim())
                .queryParam("format", "json")
                .build()
                .toUriString();

        try {
            String responseBody = webClient.get()
                    .uri(uri)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block(REQUEST_TIMEOUT);

            String formatted = formatResults(responseBody);
            log.info("Web search completed in {} ms", System.currentTimeMillis() - start);
            return formatted;
        } catch (Exception exception) {
            log.error("Web search failed for query '{}': {}", query, exception.getMessage(), exception);
            throw new RagException("Web search failed: " + exception.getMessage(), exception);
        }
    }

    private String formatResults(String responseBody) throws java.io.IOException {
        JsonNode root = objectMapper.readTree(responseBody);
        JsonNode results = root.path("results");

        if (!results.isArray() || results.isEmpty()) {
            return "No web search results found.";
        }

        int limit = Math.max(1, webSearchProperties.maxResults());
        List<String> formattedResults = new ArrayList<>();

        for (int index = 0; index < results.size() && formattedResults.size() < limit; index++) {
            JsonNode result = results.get(index);
            String title = result.path("title").asText("Untitled");
            String url = result.path("url").asText("");
            String content = result.path("content").asText("");
            formattedResults.add((formattedResults.size() + 1) + ". " + title
                    + "\nURL: " + url
                    + "\n" + content);
        }

        return String.join("\n\n", formattedResults);
    }
}
