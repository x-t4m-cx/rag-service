package com.rag.dto.openai;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * OpenAI-compatible streaming chunk response.
 *
 * @param id      completion identifier
 * @param object  object type
 * @param created creation epoch seconds
 * @param model   model identifier
 * @param choices streaming choices
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ChatCompletionChunk(
        String id,
        String object,
        long created,
        String model,
        List<ChatCompletionChoice> choices
) {
}
