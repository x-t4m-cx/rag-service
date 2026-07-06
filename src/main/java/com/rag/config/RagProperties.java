package com.rag.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for RAG pipeline settings.
 *
 * @param chunkSize            maximum size of a text chunk in characters
 * @param overlap              overlap between consecutive chunks in characters
 * @param topK                 number of top similar chunks to retrieve
 * @param similarityThreshold  minimum similarity score for chunk inclusion
 * @param async                async processing thread pool settings
 */
@ConfigurationProperties(prefix = "rag")
public record RagProperties(
        int chunkSize,
        int overlap,
        int topK,
        double similarityThreshold,
        AsyncProperties async
) {

    /**
     * Thread pool configuration for asynchronous document processing.
     *
     * @param corePoolSize  minimum number of worker threads
     * @param maxPoolSize   maximum number of worker threads
     * @param queueCapacity capacity of the task queue
     */
    public record AsyncProperties(
            int corePoolSize,
            int maxPoolSize,
            int queueCapacity
    ) {
    }
}
