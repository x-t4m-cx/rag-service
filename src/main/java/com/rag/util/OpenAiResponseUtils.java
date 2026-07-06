package com.rag.util;

import java.time.Instant;
import java.util.UUID;

/**
 * Utility methods for OpenAI-compatible API responses.
 */
public final class OpenAiResponseUtils {

    private OpenAiResponseUtils() {
    }

    /**
     * Generates an OpenAI-style chat completion identifier.
     *
     * @return completion id
     */
    public static String createCompletionId() {
        return "chatcmpl-" + UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * Returns the current epoch second timestamp.
     *
     * @return epoch seconds
     */
    public static long currentEpochSeconds() {
        return Instant.now().getEpochSecond();
    }
}
