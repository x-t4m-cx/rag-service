package com.rag.dto.openai;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * OpenAI-compatible chat completion response.
 *
 * @param id      completion identifier
 * @param object  object type
 * @param created creation epoch seconds
 * @param model   model identifier
 * @param choices completion choices
 * @param usage   token usage statistics
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ChatCompletionResponse(
        String id,
        String object,
        long created,
        String model,
        List<ChatCompletionChoice> choices,
        Usage usage
) {

    /**
     * Token usage statistics.
     *
     * @param promptTokens     prompt token count
     * @param completionTokens completion token count
     * @param totalTokens      total token count
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Usage(
            @JsonProperty("prompt_tokens") int promptTokens,
            @JsonProperty("completion_tokens") int completionTokens,
            @JsonProperty("total_tokens") int totalTokens
    ) {
    }
}
