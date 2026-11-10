package com.jobtrack.service;

import com.jobtrack.exception.BadRequestException;
import com.jobtrack.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
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

    @Test
    void storesAllowedFileWithUniqueSanitizedName() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "my resume (final).pdf", "application/pdf", "pdf-bytes".getBytes(StandardCharsets.UTF_8));

        String stored = service.storeFile(file);

        assertThat(stored).endsWith("_my_resume__final_.pdf");
        assertThat(stored).doesNotContain(" ").doesNotContain("(");
        assertThat(uploadDir.resolve(stored)).hasContent("pdf-bytes");

        Resource resource = service.loadFileAsResource(stored);
        assertThat(resource.exists()).isTrue();
    }

    @Test
    void sameNameStoredTwiceDoesNotOverwrite() {
        MockMultipartFile first = new MockMultipartFile("file", "cv.txt", "text/plain", "one".getBytes(StandardCharsets.UTF_8));
        MockMultipartFile second = new MockMultipartFile("file", "cv.txt", "text/plain", "two".getBytes(StandardCharsets.UTF_8));

        assertThat(service.storeFile(first)).isNotEqualTo(service.storeFile(second));
    }

    @Test
    void rejectsEmptyFile() {
        MockMultipartFile empty = new MockMultipartFile("file", "cv.pdf", "application/pdf", new byte[0]);

        assertThatThrownBy(() -> service.storeFile(empty))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("empty");
    }

    @Test
    void rejectsUnsupportedExtension() {
        MockMultipartFile exe = new MockMultipartFile("file", "virus.exe", "application/octet-stream", new byte[]{1, 2, 3});

        assertThatThrownBy(() -> service.storeFile(exe))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining(".exe");
    }

    @Test
    void rejectsRelativePathInOriginalName() {
        MockMultipartFile sneaky = new MockMultipartFile("file", "../../cv.pdf", "application/pdf", new byte[]{1});

        assertThatThrownBy(() -> service.storeFile(sneaky))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void deleteRemovesStoredFile() {
        String stored = service.storeFile(
                new MockMultipartFile("file", "notes.txt", "text/plain", "x".getBytes(StandardCharsets.UTF_8)));

        service.deleteFile(stored);

        assertThat(uploadDir.resolve(stored)).doesNotExist();
    }
}
