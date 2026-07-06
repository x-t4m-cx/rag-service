package com.rag.parser;

import java.io.IOException;
import java.io.InputStream;

/**
 * Parses document content from an input stream based on file extension.
 */
public interface DocumentParser {

    /**
     * Checks whether this parser supports the given file extension.
     *
     * @param extension file extension including the leading dot (e.g. {@code .pdf})
     * @return {@code true} if this parser can handle the extension
     */
    boolean supports(String extension);

    /**
     * Extracts plain text from the given input stream.
     *
     * @param stream document content stream
     * @return extracted plain text
     * @throws IOException if reading or parsing fails
     */
    String parse(InputStream stream) throws IOException;
}
