package com.jobtrack.service;

import com.jobtrack.exception.BadRequestException;
import com.jobtrack.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FileStorageServiceTest {

    @TempDir
    Path tempDir;

    private FileStorageService service;
    private Path uploadDir;

    @BeforeEach
    void setUp() {
        uploadDir = tempDir.resolve("uploads");
        service = new FileStorageService();
        ReflectionTestUtils.setField(service, "uploadDir", uploadDir.toString());
        service.init();
    }

    @Test
    void loadRejectsPathTraversal() throws Exception {
        Files.writeString(tempDir.resolve("secret.txt"), "top secret");

        assertThatThrownBy(() -> service.loadFileAsResource("../secret.txt"))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void deleteRejectsPathTraversal() throws Exception {
        Path outside = Files.writeString(tempDir.resolve("keep.txt"), "keep me");

        assertThatThrownBy(() -> service.deleteFile("../keep.txt"))
                .isInstanceOf(BadRequestException.class);
        assertThat(outside).exists();
    }

    @Test
    void loadMissingFileIsNotFound() {
        assertThatThrownBy(() -> service.loadFileAsResource("does-not-exist.pdf"))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
