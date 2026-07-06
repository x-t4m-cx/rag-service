package com.rag.controller;

import com.rag.dto.DocumentListResponse;
import com.rag.dto.DocumentUploadResponse;
import com.rag.service.DocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/documents")
@Tag(name = "Documents", description = "Document upload and management API")
public class DocumentController {

    private final DocumentService documentService;


    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Operation(summary = "Upload a document", description = "Accepts TXT, Markdown, PDF, or DOCX files up to 50 MB")
    public DocumentUploadResponse upload(@RequestPart("file") MultipartFile file) {
        return documentService.upload(file);
    }

    @GetMapping
    @Operation(summary = "List documents", description = "Returns metadata for all uploaded documents")
    public DocumentListResponse listDocuments() {
        return documentService.listDocuments();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a document", description = "Removes document metadata and vector chunks")
    public void delete(@PathVariable UUID id) {
        documentService.delete(id);
    }
}
