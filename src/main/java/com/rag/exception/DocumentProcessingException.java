package com.rag.exception;

/**
 * Thrown when document parsing, chunking, or indexing fails.
 */
public class DocumentProcessingException extends RagException {

    /**
     * Creates an exception with the given message.
     *
     * @param message human-readable error description
     */
    public DocumentProcessingException(String message) {
        super(message);
    }

    /**
     * Creates an exception with the given message and cause.
     *
     * @param message human-readable error description
     * @param cause   underlying cause
     */
    public DocumentProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
