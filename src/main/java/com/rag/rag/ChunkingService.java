package com.rag.rag;

import com.rag.config.RagProperties;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Splits document text into overlapping chunks according to configured size and overlap.
 */
@Service
public class ChunkingService {

    private final RagProperties ragProperties;

    /**
     * Creates the chunking service.
     *
     * @param ragProperties RAG pipeline configuration
     */
    public ChunkingService(RagProperties ragProperties) {
        this.ragProperties = ragProperties;
    }

    /**
     * Splits the given text into character-based chunks with configured overlap.
     *
     * @param text full document text
     * @return immutable list of non-blank chunks; empty when input is blank
     */
    public List<String> chunk(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }

        int chunkSize = ragProperties.chunkSize();
        int overlap = ragProperties.overlap();
        int step = chunkSize - overlap;

        if (step <= 0) {
            throw new IllegalStateException("Chunk overlap must be smaller than chunk size");
        }

        if (text.length() <= chunkSize) {
            return List.of(text.trim());
        }

        List<String> chunks = new ArrayList<>();
        for (int start = 0; start < text.length(); start += step) {
            int end = Math.min(start + chunkSize, text.length());
            String chunk = text.substring(start, end).trim();
            if (!chunk.isEmpty()) {
                chunks.add(chunk);
            }
            if (end >= text.length()) {
                break;
            }
        }
        return List.copyOf(chunks);
    }
}
