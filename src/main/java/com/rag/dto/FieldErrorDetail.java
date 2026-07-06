package com.rag.dto;

/**
 * Describes a single field validation error.
 *
 * @param field   name of the invalid field
 * @param message validation error message
 */
public record FieldErrorDetail(
        String field,
        String message
) {
}
