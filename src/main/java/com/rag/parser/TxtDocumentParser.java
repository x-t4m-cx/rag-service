package com.rag.parser;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Parses plain text files using UTF-8 encoding.
 */
@Component
public class TxtDocumentParser implements DocumentParser {

    private static final String EXTENSION = ".txt";

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean supports(String extension) {
        return EXTENSION.equalsIgnoreCase(extension);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String parse(InputStream stream) throws IOException {
        return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
    }
}
