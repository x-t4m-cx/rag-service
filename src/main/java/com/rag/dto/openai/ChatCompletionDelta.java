package com.rag.dto.openai;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * OpenAI-compatible streaming delta payload.
 *
 * @param role    optional role assignment
 * @param content incremental content
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ChatCompletionDelta(
        String role,
        String content
) {
}
