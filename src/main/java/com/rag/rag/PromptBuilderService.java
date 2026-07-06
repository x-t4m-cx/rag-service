package com.rag.rag;

import org.springframework.stereotype.Service;

/**
 * Builds RAG prompts from retrieved context and user questions.
 */
@Service
public class PromptBuilderService {

    private static final String TEMPLATE = """
            Ты — помощник, отвечающий исключительно по предоставленному контексту.

            Если информации недостаточно — честно сообщи об этом.

            Не выдумывай факты.

            Если возможно — указывай источник.

            Контекст:

            {context}

            Вопрос:

            {question}
            """;

    /**
     * Builds a prompt from context and a user question.
     *
     * @param context retrieved document context
     * @param question user question
     * @return formatted prompt
     */
    public String build(String context, String question) {
        return TEMPLATE
                .replace("{context}", context)
                .replace("{question}", question);
    }
}
