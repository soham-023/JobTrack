package com.jobtrack.controller;

import com.jobtrack.dto.response.DocumentResponse;
import com.jobtrack.entity.DocumentAttachment;
import com.jobtrack.entity.User;
import com.jobtrack.entity.enums.DocumentType;
import com.jobtrack.service.AuthService;
import com.jobtrack.service.DocumentService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class DocumentController {

    private final DocumentService documentService;
    private final AuthService authService;

    public DocumentController(DocumentService documentService, AuthService authService) {
        this.documentService = documentService;
        this.authService = authService;
    }

    @PostMapping(value = "/applications/{applicationId}/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentResponse> uploadDocument(
            @PathVariable Long applicationId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "documentType", defaultValue = "RESUME") DocumentType documentType
    ) {
        User currentUser = authService.getCurrentAuthenticatedUser();
        DocumentResponse response = documentService.uploadDocument(currentUser, applicationId, file, documentType);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/applications/{applicationId}/documents")
    public ResponseEntity<List<DocumentResponse>> getDocumentsByApplication(@PathVariable Long applicationId) {
        User currentUser = authService.getCurrentAuthenticatedUser();
        List<DocumentResponse> documents = documentService.getDocumentsByApplication(currentUser, applicationId);
        return ResponseEntity.ok(documents);
    }

    @GetMapping("/documents/{id}/download")
    public ResponseEntity<Resource> downloadDocument(@PathVariable Long id) {
        User currentUser = authService.getCurrentAuthenticatedUser();
        DocumentAttachment document = documentService.getDocumentEntity(currentUser, id);
        Resource resource = documentService.loadDocumentResource(document);

        MediaType mediaType;
        try {
            mediaType = MediaType.parseMediaType(document.getContentType() != null ? document.getContentType() : MediaType.APPLICATION_OCTET_STREAM_VALUE);
        } catch (Exception e) {
            mediaType = MediaType.APPLICATION_OCTET_STREAM;
        }

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + document.getOriginalFileName() + "\"")
                .body(resource);
    }

    @DeleteMapping("/documents/{id}")
    public ResponseEntity<Void> deleteDocument(@PathVariable Long id) {
        User currentUser = authService.getCurrentAuthenticatedUser();
        documentService.deleteDocument(currentUser, id);
        return ResponseEntity.noContent().build();
    }
}
