package com.jobtrack.service;

import com.jobtrack.dto.response.DocumentResponse;
import com.jobtrack.entity.DocumentAttachment;
import com.jobtrack.entity.JobApplication;
import com.jobtrack.entity.Note;
import com.jobtrack.entity.User;
import com.jobtrack.entity.enums.DocumentType;
import com.jobtrack.exception.ResourceNotFoundException;
import com.jobtrack.repository.DocumentAttachmentRepository;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DocumentService {

    private final DocumentAttachmentRepository documentAttachmentRepository;
    private final JobApplicationService jobApplicationService;
    private final FileStorageService fileStorageService;

    public DocumentService(DocumentAttachmentRepository documentAttachmentRepository,
                           JobApplicationService jobApplicationService,
                           FileStorageService fileStorageService) {
        this.documentAttachmentRepository = documentAttachmentRepository;
        this.jobApplicationService = jobApplicationService;
        this.fileStorageService = fileStorageService;
    }

    @Transactional
    public DocumentResponse uploadDocument(User user, Long applicationId, MultipartFile file, DocumentType documentType) {
        JobApplication application = jobApplicationService.findApplicationOrThrow(applicationId, user.getId());

        String storedFileName = fileStorageService.storeFile(file);
        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename() != null ? file.getOriginalFilename() : "document");

        DocumentAttachment attachment = new DocumentAttachment(
                application,
                originalFileName,
                storedFileName,
                file.getContentType(),
                file.getSize(),
                documentType != null ? documentType : DocumentType.RESUME
        );

        // Also add an audit note to the application
        Note auditNote = new Note(
                application,
                "Document Attached (" + attachment.getDocumentType() + ")",
                "Uploaded file: " + originalFileName + " (" + DocumentResponse.fromEntity(attachment).getFormattedFileSize() + ")"
        );
        application.addNote(auditNote);

        DocumentAttachment saved = documentAttachmentRepository.save(attachment);
        return DocumentResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<DocumentResponse> getDocumentsByApplication(User user, Long applicationId) {
        jobApplicationService.findApplicationOrThrow(applicationId, user.getId());

        return documentAttachmentRepository.findByJobApplicationIdAndJobApplicationUserIdOrderByUploadedAtDesc(applicationId, user.getId())
                .stream()
                .map(DocumentResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DocumentAttachment getDocumentEntity(User user, Long documentId) {
        return documentAttachmentRepository.findByIdAndJobApplicationUserId(documentId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Document not found with ID: " + documentId));
    }

    public Resource loadDocumentResource(DocumentAttachment document) {
        return fileStorageService.loadFileAsResource(document.getStoredFileName());
    }

    @Transactional
    public void deleteDocument(User user, Long documentId) {
        DocumentAttachment document = getDocumentEntity(user, documentId);
        fileStorageService.deleteFile(document.getStoredFileName());
        documentAttachmentRepository.delete(document);
    }
}
