package com.rag.dto.openai;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * OpenAI-compatible model list response.
 *
 * @param object response object type
 * @param data   available models
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ModelListResponse(
        String object,
        List<ModelData> data
) {

    /**
     * OpenAI-compatible model descriptor.
     *
     * @param id       model identifier
     * @param object   object type
     * @param created  creation epoch seconds
     * @param ownedBy  model owner
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record ModelData(
            String id,
            String object,
            long created,
            @JsonProperty("owned_by") String ownedBy
    ) {
    }
}
