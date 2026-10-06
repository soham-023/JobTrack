package com.jobtrack.repository;

import com.jobtrack.entity.Interview;
import com.jobtrack.entity.enums.InterviewStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface InterviewRepository extends JpaRepository<Interview, Long> {

    List<Interview> findByJobApplicationIdAndJobApplicationUserIdOrderByScheduledAtAsc(Long applicationId, Long userId);

    Optional<Interview> findByIdAndJobApplicationUserId(Long id, Long userId);

    long countByJobApplicationUserId(Long userId);

    long countByJobApplicationUserIdAndScheduledAtAfterAndStatus(Long userId, LocalDateTime now, InterviewStatus status);

    List<Interview> findByJobApplicationUserIdAndScheduledAtAfterOrderByScheduledAtAsc(Long userId, LocalDateTime now);
}
