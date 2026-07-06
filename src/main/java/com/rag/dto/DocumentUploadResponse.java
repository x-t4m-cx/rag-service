package com.rag.dto;

import com.rag.entity.DocumentStatus;

import java.util.UUID;

/**
 * Response returned after a document upload request is accepted.
 *
 * @param id       unique document identifier
 * @param filename original file name
 * @param status   current processing status
 */
public record DocumentUploadResponse(
        UUID id,
        String filename,
        DocumentStatus status
) {
}
