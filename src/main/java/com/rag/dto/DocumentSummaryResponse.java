package com.rag.dto;

import com.rag.entity.DocumentStatus;

import java.time.Instant;
import java.util.UUID;

/**
 * Summary information about a stored document.
 *
 * @param id           unique document identifier
 * @param filename     original file name
 * @param status       current processing status
 * @param uploadedAt   upload timestamp
 * @param chunkCount   number of indexed chunks
 * @param errorMessage optional error description when processing failed
 */
public record DocumentSummaryResponse(
        UUID id,
        String filename,
        DocumentStatus status,
        Instant uploadedAt,
        int chunkCount,
        String errorMessage
) {
}
