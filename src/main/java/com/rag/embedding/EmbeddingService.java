package com.rag.embedding;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Generates text embeddings with in-memory caching to avoid duplicate computations.
 */
@Service
public class EmbeddingService {

    private static final Logger log = LoggerFactory.getLogger(EmbeddingService.class);

    private final EmbeddingModel embeddingModel;
    private final ConcurrentMap<String, float[]> cache = new ConcurrentHashMap<>();

    /**
     * Creates the embedding service.
     *
     * @param embeddingModel Spring AI embedding model
     */
    public EmbeddingService(EmbeddingModel embeddingModel) {
        this.embeddingModel = embeddingModel;
    }

    /**
     * Generates an embedding vector for the given text, reusing cached results when available.
     *
     * @param text input text
     * @return embedding vector
     */
    public float[] embed(String text) {
        String normalized = normalize(text);
        float[] cached = cache.get(normalized);
        if (cached != null) {
            log.debug("Embedding cache hit for text of length {}", normalized.length());
            return cached;
        }

        long start = System.currentTimeMillis();
        float[] embedding = embeddingModel.embed(normalized);
        cache.put(normalized, embedding);
        log.debug("Generated embedding for text of length {} in {} ms",
                normalized.length(), System.currentTimeMillis() - start);
        return embedding;
    }

    /**
     * Generates embeddings for multiple texts, using the cache for each entry.
     *
     * @param texts input texts
     * @return embeddings in the same order as the input list
     */
    public List<float[]> embedAll(List<String> texts) {
        List<float[]> embeddings = new ArrayList<>(texts.size());
        for (String text : texts) {
            embeddings.add(embed(text));
        }
        return List.copyOf(embeddings);
    }

    private String normalize(String text) {
        return text == null ? "" : text.trim();
    }
}
