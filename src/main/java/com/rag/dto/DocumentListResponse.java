package com.rag.dto;

import java.util.List;

/**
 * Paginated-like list wrapper for document summaries.
 *
 * @param documents list of document summaries
 * @param total     total number of documents
 */
public record DocumentListResponse(
        List<DocumentSummaryResponse> documents,
        int total
) {
}
