package com.rag.parser;

import com.rag.exception.UnsupportedFileTypeException;
import com.rag.util.FileExtensionUtils;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Resolves and delegates parsing to the appropriate {@link DocumentParser}.
 */
@Component
public class DocumentParserRegistry {

    private final List<DocumentParser> parsers;

    /**
     * Creates a registry from all available document parsers.
     *
     * @param parsers injected parser implementations
     */
    public DocumentParserRegistry(List<DocumentParser> parsers) {
        this.parsers = List.copyOf(parsers);
    }

    /**
     * Parses a document using the parser that supports the given file name.
     *
     * @param filename original file name
     * @param stream   document content stream
     * @return extracted plain text
     * @throws UnsupportedFileTypeException when no parser supports the extension
     * @throws IOException                  if reading or parsing fails
     */
    public String parse(String filename, InputStream stream) throws IOException {
        String extension = FileExtensionUtils.extractExtension(filename);
        DocumentParser parser = findParser(extension)
                .orElseThrow(() -> new UnsupportedFileTypeException(extension.isBlank() ? "unknown" : extension));
        return parser.parse(stream);
    }

    /**
     * Checks whether the given file name has a supported extension.
     *
     * @param filename original file name
     * @return {@code true} when a parser is available
     */
    public boolean isSupported(String filename) {
        String extension = FileExtensionUtils.extractExtension(filename);
        return findParser(extension).isPresent();
    }

    private java.util.Optional<DocumentParser> findParser(String extension) {
        return parsers.stream()
                .filter(parser -> parser.supports(extension))
                .findFirst();
    }
}
