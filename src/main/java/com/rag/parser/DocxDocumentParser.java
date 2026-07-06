package com.rag.parser;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.stream.Collectors;

/**
 * Parses DOCX files and extracts plain text from paragraphs.
 */
@Component
public class DocxDocumentParser implements DocumentParser {

    private static final String EXTENSION = ".docx";

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
        try (XWPFDocument document = new XWPFDocument(stream)) {
            return document.getParagraphs().stream()
                    .map(XWPFParagraph::getText)
                    .filter(text -> text != null && !text.isBlank())
                    .collect(Collectors.joining(System.lineSeparator()));
        }
    }
}
