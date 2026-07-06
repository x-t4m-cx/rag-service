package com.rag.mapper;

import com.rag.dto.DocumentSummaryResponse;
import com.rag.dto.DocumentUploadResponse;
import com.rag.entity.DocumentMetadata;
import org.mapstruct.Mapper;

/**
 * MapStruct mapper for document-related DTO conversions.
 */
@Mapper(componentModel = "spring")
public interface DocumentMapper {

    /**
     * Converts metadata to an upload response.
     *
     * @param metadata document metadata
     * @return upload response
     */
    DocumentUploadResponse toUploadResponse(DocumentMetadata metadata);

    /**
     * Converts metadata to a summary response.
     *
     * @param metadata document metadata
     * @return summary response
     */
    DocumentSummaryResponse toSummary(DocumentMetadata metadata);
}
