package com.jobtrack.repository;

import com.jobtrack.entity.DocumentAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentAttachmentRepository extends JpaRepository<DocumentAttachment, Long> {

    List<DocumentAttachment> findByJobApplicationIdAndJobApplicationUserIdOrderByUploadedAtDesc(Long applicationId, Long userId);

    Optional<DocumentAttachment> findByIdAndJobApplicationUserId(Long id, Long userId);

    long countByJobApplicationUserId(Long userId);
}
