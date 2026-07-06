package com.rag.exception;

/**
 * Base runtime exception for domain-specific errors in the RAG service.
 */
public class RagException extends RuntimeException {

    /**
     * Creates a new exception with the given message.
     *
     * @param message human-readable error description
     */
    public RagException(String message) {
        super(message);
    }

    /**
     * Creates a new exception with the given message and cause.
     *
     * @param message human-readable error description
     * @param cause   underlying cause
     */
    public RagException(String message, Throwable cause) {
        super(message, cause);
    }
}
