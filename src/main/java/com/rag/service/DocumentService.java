package com.rag.service;

import com.rag.dto.DocumentListResponse;
import com.rag.dto.DocumentUploadResponse;
import com.rag.entity.DocumentMetadata;
import com.rag.exception.DocumentNotFoundException;
import com.rag.exception.UnsupportedFileTypeException;
import com.rag.mapper.DocumentMapper;
import com.rag.parser.DocumentParserRegistry;
import com.rag.repository.DocumentMetadataRepository;
import com.rag.vector.VectorStoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

/**
 * Application service for document upload, listing, and deletion.
 */
@Service
public class DocumentService {

    private static final Logger log = LoggerFactory.getLogger(DocumentService.class);

    private final DocumentMetadataRepository metadataRepository;
    private final DocumentParserRegistry parserRegistry;
    private final DocumentProcessingService documentProcessingService;
    private final VectorStoreService vectorStoreService;
    private final DocumentMapper documentMapper;

    /**
     * Creates the document service.
     *
     * @param metadataRepository         document metadata repository
     * @param parserRegistry             document parser registry
     * @param documentProcessingService  async processing service
     * @param vectorStoreService         vector store adapter
     * @param documentMapper             DTO mapper
     */
    public DocumentService(DocumentMetadataRepository metadataRepository,
                           DocumentParserRegistry parserRegistry,
                           DocumentProcessingService documentProcessingService,
                           VectorStoreService vectorStoreService,
                           DocumentMapper documentMapper) {
        this.metadataRepository = metadataRepository;
        this.parserRegistry = parserRegistry;
        this.documentProcessingService = documentProcessingService;
        this.vectorStoreService = vectorStoreService;
        this.documentMapper = documentMapper;
    }

    /**
     * Accepts a document upload and triggers asynchronous indexing.
     *
     * @param file uploaded multipart file
     * @return accepted upload response
     */
    public DocumentUploadResponse upload(MultipartFile file) {
        String filename = file.getOriginalFilename();
        if (filename == null || filename.isBlank()) {
            throw new UnsupportedFileTypeException("unknown");
        }
        if (!parserRegistry.isSupported(filename)) {
            throw new UnsupportedFileTypeException(filename);
        }

        UUID documentId = UUID.randomUUID();
        DocumentMetadata metadata = metadataRepository.save(DocumentMetadata.processing(documentId, filename));

        byte[] content;
        try {
            content = file.getBytes();
        } catch (IOException exception) {
            metadataRepository.save(metadata.failed("Failed to read uploaded file"));
            throw new com.rag.exception.DocumentProcessingException("Failed to read uploaded file", exception);
        }

        log.info("Accepted document upload: id={}, filename={}, size={} bytes",
                documentId, filename, content.length);
        documentProcessingService.processAsync(documentId, filename, content);
        return documentMapper.toUploadResponse(metadata);
    }

    /**
     * Returns all uploaded documents.
     *
     * @return document list response
     */
    public DocumentListResponse listDocuments() {
        var summaries = metadataRepository.findAll().stream()
                .map(documentMapper::toSummary)
                .toList();
        return new DocumentListResponse(summaries, summaries.size());
    }

    /**
     * Deletes a document and its indexed chunks.
     *
     * @param id document identifier
     */
    public void delete(UUID id) {
        DocumentMetadata metadata = metadataRepository.findById(id)
                .orElseThrow(() -> new DocumentNotFoundException(id));

        vectorStoreService.deleteByDocumentId(id);
        metadataRepository.deleteById(id);
        log.info("Deleted document {} ({})", id, metadata.filename());
    }
}
