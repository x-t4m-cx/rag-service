package com.rag.service;

import com.rag.exception.DocumentNotFoundException;
import com.rag.exception.DocumentProcessingException;
import com.rag.exception.UnsupportedFileTypeException;
import com.rag.parser.DocumentParserRegistry;
import com.rag.rag.ChunkingService;
import com.rag.repository.DocumentMetadataRepository;
import com.rag.entity.DocumentMetadata;
import com.rag.vector.VectorStoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * Orchestrates asynchronous document ingestion: parse, chunk, embed, and index.
 */
@Service
public class DocumentProcessingService {

    private static final Logger log = LoggerFactory.getLogger(DocumentProcessingService.class);

    private final DocumentParserRegistry parserRegistry;
    private final ChunkingService chunkingService;
    private final VectorStoreService vectorStoreService;
    private final DocumentMetadataRepository metadataRepository;
    private final Executor documentProcessingExecutor;

    /**
     * Creates the document processing service.
     *
     * @param parserRegistry             document parser registry
     * @param chunkingService            text chunking service
     * @param vectorStoreService         vector store adapter
     * @param metadataRepository         document metadata repository
     * @param documentProcessingExecutor async executor for indexing tasks
     */
    public DocumentProcessingService(DocumentParserRegistry parserRegistry,
                                     ChunkingService chunkingService,
                                     VectorStoreService vectorStoreService,
                                     DocumentMetadataRepository metadataRepository,
                                     @Qualifier("documentProcessingExecutor") Executor documentProcessingExecutor) {
        this.parserRegistry = parserRegistry;
        this.chunkingService = chunkingService;
        this.vectorStoreService = vectorStoreService;
        this.metadataRepository = metadataRepository;
        this.documentProcessingExecutor = documentProcessingExecutor;
    }

    /**
     * Starts asynchronous processing for an uploaded document.
     *
     * @param documentId document identifier
     * @param filename   original file name
     * @param content    raw file bytes
     * @return future that completes when processing finishes
     */
    public CompletableFuture<Void> processAsync(UUID documentId, String filename, byte[] content) {
        return CompletableFuture.runAsync(
                () -> process(documentId, filename, content),
                documentProcessingExecutor
        );
    }

    private void process(UUID documentId, String filename, byte[] content) {
        DocumentMetadata metadata = metadataRepository.findById(documentId)
                .orElseThrow(() -> new DocumentNotFoundException(documentId));

        long start = System.currentTimeMillis();
        log.info("Started processing document {} ({})", documentId, filename);

        try {
            String text = parseDocument(filename, content);
            var chunks = chunkingService.chunk(text);

            if (chunks.isEmpty()) {
                throw new DocumentProcessingException("Document contains no indexable text");
            }

            vectorStoreService.indexChunks(documentId, filename, chunks, metadata.uploadedAt());
            metadataRepository.save(metadata.completed(chunks.size()));

            log.info("Completed processing document {} with {} chunk(s) in {} ms",
                    documentId, chunks.size(), System.currentTimeMillis() - start);
        } catch (Exception exception) {
            log.error("Failed to process document {}: {}", documentId, exception.getMessage(), exception);
            metadataRepository.save(metadata.failed(exception.getMessage()));
        }
    }

    private String parseDocument(String filename, byte[] content) throws IOException {
        if (!parserRegistry.isSupported(filename)) {
            throw new UnsupportedFileTypeException(filename);
        }

        long start = System.currentTimeMillis();
        String text;
        try (ByteArrayInputStream stream = new ByteArrayInputStream(content)) {
            text = parserRegistry.parse(filename, stream);
        }
        log.info("Parsed document {} in {} ms", filename, System.currentTimeMillis() - start);
        return text;
    }
}
