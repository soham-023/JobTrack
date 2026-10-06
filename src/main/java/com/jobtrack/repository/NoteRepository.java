package com.jobtrack.repository;

import com.jobtrack.entity.Note;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NoteRepository extends JpaRepository<Note, Long> {

    List<Note> findByJobApplicationIdAndJobApplicationUserIdOrderByCreatedAtDesc(Long applicationId, Long userId);

    Optional<Note> findByIdAndJobApplicationUserId(Long id, Long userId);
}
