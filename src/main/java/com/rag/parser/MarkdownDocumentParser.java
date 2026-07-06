package com.rag.parser;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.text.TextContentRenderer;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Parses Markdown files and converts them to plain text.
 */
@Component
public class MarkdownDocumentParser implements DocumentParser {

    private static final String EXTENSION_MD = ".md";
    private static final String EXTENSION_MARKDOWN = ".markdown";

    private final Parser markdownParser = Parser.builder().build();
    private final TextContentRenderer textRenderer = TextContentRenderer.builder().build();

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean supports(String extension) {
        return EXTENSION_MD.equalsIgnoreCase(extension)
                || EXTENSION_MARKDOWN.equalsIgnoreCase(extension);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String parse(InputStream stream) throws IOException {
        String markdown = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        Node document = markdownParser.parse(markdown);
        return textRenderer.render(document);
    }
}
