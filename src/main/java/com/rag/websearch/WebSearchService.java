package com.rag.websearch;

/**
 * Abstraction for web search providers used by the LLM tool-calling layer.
 */
public interface WebSearchService {

    /**
     * Executes a web search for the given query.
     *
     * @param query search query
     * @return formatted search results for the LLM
     */
    String search(String query);
}
