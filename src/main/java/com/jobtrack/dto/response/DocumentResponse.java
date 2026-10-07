package com.jobtrack.dto.response;

import com.jobtrack.entity.DocumentAttachment;
import com.jobtrack.entity.enums.DocumentType;

import java.time.LocalDateTime;

public class DocumentResponse {

    private Long id;
    private Long applicationId;
    private String originalFileName;
    private String contentType;
    private Long fileSize;
    private String formattedFileSize;
    private DocumentType documentType;
    private LocalDateTime uploadedAt;
    private String downloadUrl;

    public DocumentResponse() {
    }

    public static DocumentResponse fromEntity(DocumentAttachment doc) {
        if (doc == null) return null;
        DocumentResponse response = new DocumentResponse();
        response.setId(doc.getId());
        if (doc.getJobApplication() != null) {
            response.setApplicationId(doc.getJobApplication().getId());
        }
        response.setOriginalFileName(doc.getOriginalFileName());
        response.setContentType(doc.getContentType());
        response.setFileSize(doc.getFileSize());
        response.setFormattedFileSize(formatFileSize(doc.getFileSize()));
        response.setDocumentType(doc.getDocumentType());
        response.setUploadedAt(doc.getUploadedAt());
        response.setDownloadUrl("/api/v1/documents/" + doc.getId() + "/download");
        return response;
    }

    private static String formatFileSize(Long bytes) {
        if (bytes == null || bytes == 0) return "0 B";
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        char pre = "KMGTPE".charAt(exp - 1);
        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(Long applicationId) {
        this.applicationId = applicationId;
    }

    public String getOriginalFileName() {
        return originalFileName;
    }

    public void setOriginalFileName(String originalFileName) {
        this.originalFileName = originalFileName;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getFormattedFileSize() {
        return formattedFileSize;
    }

    public void setFormattedFileSize(String formattedFileSize) {
        this.formattedFileSize = formattedFileSize;
    }

    public DocumentType getDocumentType() {
        return documentType;
    }

    public void setDocumentType(DocumentType documentType) {
        this.documentType = documentType;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }
}
