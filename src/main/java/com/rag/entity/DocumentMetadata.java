package com.rag.entity;

import java.time.Instant;
import java.util.UUID;

/**
 * Metadata describing an uploaded and indexed document.
 *
 * @param id          unique document identifier
 * @param filename    original file name
 * @param status      current processing status
 * @param uploadedAt  upload timestamp
 * @param chunkCount  number of indexed chunks
 * @param errorMessage optional error description when status is {@link DocumentStatus#FAILED}
 */
public record DocumentMetadata(
        UUID id,
        String filename,
        DocumentStatus status,
        Instant uploadedAt,
        int chunkCount,
        String errorMessage
) {

    /**
     * Creates metadata for a newly uploaded document.
     *
     * @param id       unique document identifier
     * @param filename original file name
     * @return metadata with {@link DocumentStatus#PROCESSING} status
     */
    public static DocumentMetadata processing(UUID id, String filename) {
        return new DocumentMetadata(id, filename, DocumentStatus.PROCESSING, Instant.now(), 0, null);
    }

    /**
     * Returns a copy marked as successfully completed.
     *
     * @param chunkCount number of indexed chunks
     * @return updated metadata
     */
    public DocumentMetadata completed(int chunkCount) {
        return new DocumentMetadata(id, filename, DocumentStatus.COMPLETED, uploadedAt, chunkCount, null);
    }

    /**
     * Returns a copy marked as failed.
     *
     * @param errorMessage failure description
     * @return updated metadata
     */
    public DocumentMetadata failed(String errorMessage) {
        return new DocumentMetadata(id, filename, DocumentStatus.FAILED, uploadedAt, chunkCount, errorMessage);
    }
}
