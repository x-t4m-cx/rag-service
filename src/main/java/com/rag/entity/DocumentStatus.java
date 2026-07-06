package com.rag.entity;

/**
 * Lifecycle status of an uploaded document.
 */
public enum DocumentStatus {

    /** Document is being parsed, chunked, and indexed. */
    PROCESSING,

    /** Document has been successfully indexed. */
    COMPLETED,

    /** Document processing failed. */
    FAILED
}
