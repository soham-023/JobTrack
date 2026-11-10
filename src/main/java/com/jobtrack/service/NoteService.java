package com.jobtrack.service;

import com.jobtrack.dto.request.CreateNoteRequest;
import com.jobtrack.dto.request.UpdateNoteRequest;
import com.jobtrack.dto.response.NoteResponse;
import com.jobtrack.entity.JobApplication;
import com.jobtrack.entity.Note;
import com.jobtrack.entity.User;
import com.jobtrack.exception.ResourceNotFoundException;
import com.jobtrack.repository.NoteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NoteService {

    static final String DEFAULT_NOTE_TITLE = "General Note";

    private final NoteRepository noteRepository;
    private final JobApplicationService jobApplicationService;

    public NoteService(NoteRepository noteRepository, JobApplicationService jobApplicationService) {
        this.noteRepository = noteRepository;
        this.jobApplicationService = jobApplicationService;
    }

    @Transactional
    public NoteResponse addNote(User user, Long applicationId, CreateNoteRequest request) {
        JobApplication application = jobApplicationService.findApplicationOrThrow(applicationId, user.getId());

        Note note = new Note();
        note.setJobApplication(application);
        note.setTitle(hasText(request.getTitle()) ? request.getTitle().trim() : DEFAULT_NOTE_TITLE);
        note.setContent(request.getContent().trim());

        Note saved = noteRepository.save(note);
        return NoteResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<NoteResponse> getNotesByApplication(User user, Long applicationId) {
        jobApplicationService.findApplicationOrThrow(applicationId, user.getId());

        return noteRepository.findByJobApplicationIdAndJobApplicationUserIdOrderByCreatedAtDesc(applicationId, user.getId())
                .stream()
                .map(NoteResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public NoteResponse updateNote(User user, Long noteId, UpdateNoteRequest request) {
        Note note = findNoteOrThrow(noteId, user.getId());

        // A missing or blank title keeps the existing one rather than wiping it out.
        if (hasText(request.getTitle())) {
            note.setTitle(request.getTitle().trim());
        }
        note.setContent(request.getContent().trim());

        Note updated = noteRepository.save(note);
        return NoteResponse.fromEntity(updated);
    }

    @Transactional
    public void deleteNote(User user, Long noteId) {
        Note note = findNoteOrThrow(noteId, user.getId());
        noteRepository.delete(note);
    }

    public Note findNoteOrThrow(Long noteId, Long userId) {
        return noteRepository.findByIdAndJobApplicationUserId(noteId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Note not found with ID: " + noteId));
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
