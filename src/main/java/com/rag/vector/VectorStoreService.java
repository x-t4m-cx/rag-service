package com.rag.vector;

import com.rag.config.RagProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Adapter around Spring AI {@link VectorStore} for document chunk indexing and retrieval.
 */
@Service
public class VectorStoreService {

    private static final Logger log = LoggerFactory.getLogger(VectorStoreService.class);

    private final VectorStore vectorStore;
    private final RagProperties ragProperties;

    /**
     * Creates the vector store service.
     *
     * @param vectorStore   Spring AI vector store
     * @param ragProperties RAG configuration
     */
    public VectorStoreService(VectorStore vectorStore, RagProperties ragProperties) {
        this.vectorStore = vectorStore;
        this.ragProperties = ragProperties;
    }

    /**
     * Indexes document chunks in the vector store.
     *
     * @param documentId document identifier
     * @param filename   original file name
     * @param chunks     chunk texts
     * @param uploadedAt upload timestamp
     */
    public void indexChunks(UUID documentId, String filename, List<String> chunks, Instant uploadedAt) {
        if (chunks.isEmpty()) {
            log.warn("No chunks to index for document {}", documentId);
            return;
        }

        List<Document> documents = new java.util.ArrayList<>(chunks.size());
        for (int index = 0; index < chunks.size(); index++) {
            String chunkText = chunks.get(index);
            documents.add(Document.builder()
                    .id(buildChunkId(documentId, index))
                    .text(chunkText)
                    .metadata(java.util.Map.of(
                            VectorMetadataKeys.DOCUMENT_ID, documentId.toString(),
                            VectorMetadataKeys.FILENAME, filename,
                            VectorMetadataKeys.CHUNK_INDEX, index,
                            VectorMetadataKeys.CHUNK_TEXT, chunkText,
                            VectorMetadataKeys.UPLOADED_AT, uploadedAt.toString()
                    ))
                    .build());
        }

        long start = System.currentTimeMillis();
        vectorStore.add(documents);
        log.info("Indexed {} chunk(s) for document {} in {} ms",
                chunks.size(), documentId, System.currentTimeMillis() - start);
    }

    /**
     * Performs a similarity search for the given query.
     *
     * @param query natural language query
     * @return matching documents ordered by relevance
     */
    public List<Document> similaritySearch(String query) {
        SearchRequest request = SearchRequest.builder()
                .query(query)
                .topK(ragProperties.topK())
                .similarityThreshold(ragProperties.similarityThreshold())
                .build();

        long start = System.currentTimeMillis();
        List<Document> results = vectorStore.similaritySearch(request);
        log.info("Vector search returned {} result(s) in {} ms", results.size(), System.currentTimeMillis() - start);
        return results;
    }

    /**
     * Deletes all chunks associated with the given document.
     *
     * @param documentId document identifier
     */
    public void deleteByDocumentId(UUID documentId) {
        FilterExpressionBuilder builder = new FilterExpressionBuilder();
        vectorStore.delete(builder.eq(VectorMetadataKeys.DOCUMENT_ID, documentId.toString()).build());
        log.info("Deleted vector chunks for document {}", documentId);
    }

    /**
     * Builds a stable chunk identifier used in the vector store.
     *
     * @param documentId document identifier
     * @param chunkIndex zero-based chunk index
     * @return chunk identifier
     */
    public static String buildChunkId(UUID documentId, int chunkIndex) {
        return documentId + ":" + chunkIndex;
    }
}
