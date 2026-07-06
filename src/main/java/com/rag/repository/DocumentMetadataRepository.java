package com.rag.repository;

import com.rag.entity.DocumentMetadata;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Persistence port for document metadata.
 */
public interface DocumentMetadataRepository {

    /**
     * Saves or updates document metadata.
     *
     * @param metadata document metadata
     * @return saved metadata
     */
    DocumentMetadata save(DocumentMetadata metadata);

    /**
     * Finds metadata by document identifier.
     *
     * @param id document identifier
     * @return metadata when present
     */
    Optional<DocumentMetadata> findById(UUID id);

    /**
     * Returns all stored document metadata ordered by upload time descending.
     *
     * @return document metadata list
     */
    List<DocumentMetadata> findAll();

    /**
     * Deletes metadata for the given document.
     *
     * @param id document identifier
     * @return {@code true} when a record was removed
     */
    boolean deleteById(UUID id);
}
