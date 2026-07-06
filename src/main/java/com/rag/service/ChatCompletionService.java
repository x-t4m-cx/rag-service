package com.rag.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rag.config.OpenAiApiProperties;
import com.rag.dto.openai.ChatCompletionChoice;
import com.rag.dto.openai.ChatCompletionChunk;
import com.rag.dto.openai.ChatCompletionDelta;
import com.rag.dto.openai.ChatCompletionRequest;
import com.rag.dto.openai.ChatCompletionResponse;
import com.rag.dto.openai.ChatMessage;
import com.rag.dto.openai.ModelListResponse;
import com.rag.exception.RagException;
import com.rag.rag.RagService;
import com.rag.util.OpenAiResponseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * Builds OpenAI-compatible chat completion and model list responses.
 */
@Service
public class ChatCompletionService {

    private static final Logger log = LoggerFactory.getLogger(ChatCompletionService.class);

    private final RagService ragService;
    private final OpenAiApiProperties openAiApiProperties;
    private final ObjectMapper objectMapper;

    /**
     * Creates the chat completion service.
     *
     * @param ragService          RAG pipeline service
     * @param openAiApiProperties OpenAI API configuration
     * @param objectMapper        JSON mapper
     */
    public ChatCompletionService(RagService ragService,
                                 OpenAiApiProperties openAiApiProperties,
                                 ObjectMapper objectMapper) {
        this.ragService = ragService;
        this.openAiApiProperties = openAiApiProperties;
        this.objectMapper = objectMapper;
    }

    /**
     * Returns the configured model list.
     *
     * @return OpenAI-compatible model list
     */
    public ModelListResponse listModels() {
        ModelListResponse.ModelData model = new ModelListResponse.ModelData(
                openAiApiProperties.defaultModel(),
                "model",
                OpenAiResponseUtils.currentEpochSeconds(),
                openAiApiProperties.modelOwner()
        );
        return new ModelListResponse("list", List.of(model));
    }

    /**
     * Generates a non-streaming chat completion.
     *
     * @param request OpenAI-compatible request
     * @return completion response
     */
    public ChatCompletionResponse complete(ChatCompletionRequest request) {
        String model = resolveModel(request.model());
        String completionId = OpenAiResponseUtils.createCompletionId();
        String question = extractLastUserMessage(request.messages());
        long created = OpenAiResponseUtils.currentEpochSeconds();

        log.info("Processing chat completion id={} model={}", completionId, model);
        String answer = ragService.answer(question);

        ChatCompletionChoice choice = new ChatCompletionChoice(
                0,
                new ChatMessage("assistant", answer),
                null,
                "stop"
        );
        ChatCompletionResponse.Usage usage = new ChatCompletionResponse.Usage(0, 0, 0);
        return new ChatCompletionResponse(completionId, "chat.completion", created, model, List.of(choice), usage);
    }

    /**
     * Generates a streaming chat completion as Server-Sent Events.
     *
     * @param request OpenAI-compatible request
     * @return SSE flux compatible with OpenAI streaming format
     */
    public Flux<ServerSentEvent<String>> stream(ChatCompletionRequest request) {
        String model = resolveModel(request.model());
        String completionId = OpenAiResponseUtils.createCompletionId();
        String question = extractLastUserMessage(request.messages());
        long created = OpenAiResponseUtils.currentEpochSeconds();

        log.info("Processing streaming chat completion id={} model={}", completionId, model);

        Flux<ServerSentEvent<String>> contentEvents = ragService.answerStream(question)
                .map(chunk -> toChunkEvent(completionId, model, created, chunk, null))
                .concatWith(Flux.just(toChunkEvent(completionId, model, created, null, "stop")));

        return Flux.concat(
                Flux.just(toChunkEvent(completionId, model, created, null, null, "assistant")),
                contentEvents,
                Flux.just(ServerSentEvent.builder("[DONE]").build())
        );
    }

    private ServerSentEvent<String> toChunkEvent(String completionId,
                                                 String model,
                                                 long created,
                                                 String content,
                                                 String finishReason) {
        return toChunkEvent(completionId, model, created, content, finishReason, null);
    }

    private ServerSentEvent<String> toChunkEvent(String completionId,
                                                 String model,
                                                 long created,
                                                 String content,
                                                 String finishReason,
                                                 String role) {
        ChatCompletionDelta delta = new ChatCompletionDelta(role, content);
        ChatCompletionChoice choice = new ChatCompletionChoice(0, null, delta, finishReason);
        ChatCompletionChunk chunk = new ChatCompletionChunk(
                completionId,
                "chat.completion.chunk",
                created,
                model,
                List.of(choice)
        );
        return ServerSentEvent.builder(toJson(chunk)).build();
    }

    private String toJson(ChatCompletionChunk chunk) {
        try {
            return objectMapper.writeValueAsString(chunk);
        } catch (JsonProcessingException exception) {
            throw new RagException("Failed to serialize streaming chunk", exception);
        }
    }

    private String resolveModel(String requestedModel) {
        return requestedModel != null && !requestedModel.isBlank()
                ? requestedModel
                : openAiApiProperties.defaultModel();
    }

    private String extractLastUserMessage(List<ChatMessage> messages) {
        for (int index = messages.size() - 1; index >= 0; index--) {
            ChatMessage message = messages.get(index);
            if ("user".equalsIgnoreCase(message.role())) {
                return message.content();
            }
        }
        throw new RagException("At least one user message is required");
    }
}
