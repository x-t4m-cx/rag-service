package com.rag.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;

/**
 * Unified error response returned by the global exception handler.
 *
 * @param timestamp moment when the error occurred
 * @param status    HTTP status code
 * @param error     HTTP status reason phrase
 * @param message   human-readable error description
 * @param path      request path that caused the error
 * @param details   optional field-level validation errors
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record ErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        List<FieldErrorDetail> details
) {

    /**
     * Creates an error response without validation details.
     *
     * @param status  HTTP status code
     * @param error   HTTP status reason phrase
     * @param message human-readable error description
     * @param path    request path
     * @return error response instance
     */
    public static ErrorResponse of(int status, String error, String message, String path) {
        return new ErrorResponse(Instant.now(), status, error, message, path, List.of());
    }

    /**
     * Creates an error response with validation details.
     *
     * @param status  HTTP status code
     * @param error   HTTP status reason phrase
     * @param message human-readable error description
     * @param path    request path
     * @param details field-level validation errors
     * @return error response instance
     */
    public static ErrorResponse of(int status, String error, String message, String path,
                                   List<FieldErrorDetail> details) {
        return new ErrorResponse(Instant.now(), status, error, message, path, details);
    }
}
