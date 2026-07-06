package com.rag.repository;

import com.rag.entity.DocumentMetadata;
import org.springframework.stereotype.Repository;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Thread-safe in-memory implementation of {@link DocumentMetadataRepository}.
 */
@Repository
public class InMemoryDocumentMetadataRepository implements DocumentMetadataRepository {

    private final ConcurrentMap<UUID, DocumentMetadata> store = new ConcurrentHashMap<>();

    @Override
    public DocumentMetadata save(DocumentMetadata metadata) {
        store.put(metadata.id(), metadata);
        return metadata;
    }

    @Override
    public Optional<DocumentMetadata> findById(UUID id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<DocumentMetadata> findAll() {
        return store.values().stream()
                .sorted(Comparator.comparing(DocumentMetadata::uploadedAt).reversed())
                .toList();
    }

    @Override
    public boolean deleteById(UUID id) {
        return store.remove(id) != null;
    }
}
