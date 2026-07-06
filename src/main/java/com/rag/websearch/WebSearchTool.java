package com.rag.websearch;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Spring AI tool that allows the LLM to request web search when local context is insufficient.
 */
@Component
@ConditionalOnProperty(name = "rag.web-search.enabled", havingValue = "true")
public class WebSearchTool {

    private final WebSearchService webSearchService;

    /**
     * Creates the web search tool.
     *
     * @param webSearchService web search provider
     */
    public WebSearchTool(WebSearchService webSearchService) {
        this.webSearchService = webSearchService;
    }

    /**
     * Searches the web for up-to-date information.
     *
     * @param query search query formulated by the LLM
     * @return formatted search results
     */
    @Tool(name = "web_search", description = """
            Search the web for current or external information when the provided document context \
            is insufficient or the question requires up-to-date data.""")
    public String webSearch(@ToolParam(description = "Concise search query") String query) {
        return webSearchService.search(query);
    }
}
