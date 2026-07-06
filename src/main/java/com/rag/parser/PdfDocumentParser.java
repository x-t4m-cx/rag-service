package com.rag.parser;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;

/**
 * Parses PDF files and extracts plain text content.
 */
@Component
public class PdfDocumentParser implements DocumentParser {

    private static final String EXTENSION = ".pdf";

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
        byte[] content = stream.readAllBytes();
        try (PDDocument document = Loader.loadPDF(content)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }
}
