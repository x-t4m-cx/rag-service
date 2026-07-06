package com.rag.dto.openai;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;

/**
 * OpenAI-compatible chat message.
 *
 * @param role    message role (system, user, assistant)
 * @param content message text content
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ChatMessage(
        @NotBlank String role,
        @NotBlank String content
) {
}
