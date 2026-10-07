package com.jobtrack.service;

import com.jobtrack.exception.BadRequestException;
import com.jobtrack.exception.ResourceNotFoundException;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${application.storage.upload-dir:uploads}")
    private String uploadDir;

    private Path storageLocation;

    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
            "pdf", "doc", "docx", "txt", "rtf", "png", "jpg", "jpeg"
    );

    private static final long MAX_FILE_SIZE = 15 * 1024 * 1024; // 15 MB

    @PostConstruct
    public void init() {
        this.storageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.storageLocation);
        } catch (IOException ex) {
            throw new RuntimeException("Could not create the upload directory at " + storageLocation, ex);
        }
    }

    public String storeFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Cannot upload an empty file");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BadRequestException("File size exceeds maximum limit of 15 MB");
        }

        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename() != null ? file.getOriginalFilename() : "document");

        if (originalFileName.contains("..")) {
            throw new BadRequestException("Filename contains invalid relative path sequence: " + originalFileName);
        }

        String fileExtension = getFileExtension(originalFileName).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(fileExtension)) {
            throw new BadRequestException("File type '." + fileExtension + "' is not supported. Allowed types: " + ALLOWED_EXTENSIONS);
        }

        String storedFileName = UUID.randomUUID() + "_" + originalFileName.replaceAll("[^a-zA-Z0-9._-]", "_");

        try {
            Path targetLocation = this.storageLocation.resolve(storedFileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            return storedFileName;
        } catch (IOException ex) {
            throw new RuntimeException("Could not store file " + originalFileName + ". Please try again!", ex);
        }
    }

    public Resource loadFileAsResource(String storedFileName) {
        try {
            Path filePath = this.storageLocation.resolve(storedFileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new ResourceNotFoundException("File not found: " + storedFileName);
            }
        } catch (MalformedURLException ex) {
            throw new ResourceNotFoundException("File not found: " + storedFileName);
        }
    }

    public void deleteFile(String storedFileName) {
        try {
            Path filePath = this.storageLocation.resolve(storedFileName).normalize();
            Files.deleteIfExists(filePath);
        } catch (IOException ex) {
            // Log and ignore to prevent blocking database record deletion
        }
    }

    private String getFileExtension(String fileName) {
        int lastIndex = fileName.lastIndexOf('.');
        if (lastIndex == -1 || lastIndex == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(lastIndex + 1);
    }
}
