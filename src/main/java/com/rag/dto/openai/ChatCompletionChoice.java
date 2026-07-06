package com.rag.dto.openai;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * OpenAI-compatible chat completion choice.
 *
 * @param index         choice index
 * @param message       assistant message for non-streaming responses
 * @param delta         incremental delta for streaming responses
 * @param finishReason  completion finish reason
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ChatCompletionChoice(
        int index,
        ChatMessage message,
        ChatCompletionDelta delta,
        @JsonProperty("finish_reason") String finishReason
) {
}
