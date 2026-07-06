package com.rag.controller;

import com.rag.dto.openai.ChatCompletionRequest;
import com.rag.dto.openai.ModelListResponse;
import com.rag.service.ChatCompletionService;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
@Hidden
public class ChatController {

    private final ChatCompletionService chatCompletionService;

    @PostMapping(value = "/chat/completions", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.TEXT_EVENT_STREAM_VALUE})
    public Object createChatCompletion(@Valid @RequestBody ChatCompletionRequest request) {
        if (request.isStreaming()) {
            return chatCompletionService.stream(request);
        }
        return chatCompletionService.complete(request);
    }

    @GetMapping("/models")
    public ModelListResponse listModels() {
        return chatCompletionService.listModels();
    }
}
