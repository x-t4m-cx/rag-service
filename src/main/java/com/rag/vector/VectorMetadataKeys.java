package com.rag.vector;

/**
 * Metadata keys stored alongside vector chunks in Qdrant.
 */
public final class VectorMetadataKeys {

    /** Unique document identifier. */
    public static final String DOCUMENT_ID = "documentId";

    /** Original uploaded file name. */
    public static final String FILENAME = "filename";

    /** Zero-based chunk index within the document. */
    public static final String CHUNK_INDEX = "chunkIndex";

    /** Chunk text content. */
    public static final String CHUNK_TEXT = "chunkText";

    /** Document upload timestamp as ISO-8601 string. */
    public static final String UPLOADED_AT = "uploadedAt";

    private VectorMetadataKeys() {
    }
}
