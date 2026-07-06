package com.rag.dto.openai;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * OpenAI-compatible chat completion request.
 *
 * @param model    model identifier
 * @param messages conversation messages
 * @param stream   whether to stream the response via SSE
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ChatCompletionRequest(
        String model,
        @NotEmpty @Valid List<ChatMessage> messages,
        Boolean stream
) {

    /**
     * Returns whether streaming is requested.
     *
     * @return {@code true} when stream flag is explicitly set to true
     */
    public boolean isStreaming() {
        return Boolean.TRUE.equals(stream);
    }
}
