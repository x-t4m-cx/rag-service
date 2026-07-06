package com.rag.exception;

/**
 * Thrown when an uploaded file has an unsupported extension.
 */
public class UnsupportedFileTypeException extends RagException {

    /**
     * Creates an exception for the given file extension.
     *
     * @param extension unsupported file extension
     */
    public UnsupportedFileTypeException(String extension) {
        super("Unsupported file type: " + extension);
    }
}
