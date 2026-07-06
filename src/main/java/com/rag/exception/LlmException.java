package com.rag.exception;

/**
 * Thrown when an LLM request fails or returns an invalid response.
 */
public class LlmException extends RagException {

    /**
     * Creates an exception with the given message.
     *
     * @param message human-readable error description
     */
    public LlmException(String message) {
        super(message);
    }

    /**
     * Creates an exception with the given message and cause.
     *
     * @param message human-readable error description
     * @param cause   underlying cause
     */
    public LlmException(String message, Throwable cause) {
        super(message, cause);
    }
}
