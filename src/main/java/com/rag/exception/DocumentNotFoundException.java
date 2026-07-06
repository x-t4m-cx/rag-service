package com.rag.exception;

import java.util.UUID;

/**
 * Thrown when a requested document cannot be found.
 */
public class DocumentNotFoundException extends RagException {

    /**
     * Creates an exception for the given document identifier.
     *
     * @param documentId identifier of the missing document
     */
    public DocumentNotFoundException(UUID documentId) {
        super("Document not found: " + documentId);
    }
}
