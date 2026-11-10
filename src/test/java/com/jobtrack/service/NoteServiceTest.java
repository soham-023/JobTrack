package com.jobtrack.service;

import com.jobtrack.dto.request.CreateNoteRequest;
import com.jobtrack.dto.request.UpdateNoteRequest;
import com.jobtrack.dto.response.NoteResponse;
import com.jobtrack.entity.JobApplication;
import com.jobtrack.entity.Note;
import com.jobtrack.entity.User;
import com.jobtrack.entity.enums.Role;
import com.jobtrack.exception.ResourceNotFoundException;
import com.jobtrack.repository.NoteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NoteServiceTest {

    @Mock
    private NoteRepository noteRepository;

    @Mock
    private JobApplicationService jobApplicationService;

    @InjectMocks
    private NoteService noteService;

    private User user;
    private JobApplication application;

    @BeforeEach
    void setUp() {
        user = new User("notes@example.com", "pass", "Note Taker", Role.ROLE_USER);
        user.setId(7L);
        application = new JobApplication();
        application.setId(42L);
        application.setUser(user);
    }

    private void saveReturnsArgument() {
        when(noteRepository.save(any(Note.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void addNoteTrimsInputAndLinksApplication() {
        when(jobApplicationService.findApplicationOrThrow(42L, 7L)).thenReturn(application);
        saveReturnsArgument();

        NoteResponse response = noteService.addNote(user, 42L, new CreateNoteRequest("  Recruiter call ", "  Went well  "));

        assertThat(response.getTitle()).isEqualTo("Recruiter call");
        assertThat(response.getContent()).isEqualTo("Went well");
        assertThat(response.getApplicationId()).isEqualTo(42L);
    }

    @Test
    void addNoteWithBlankTitleUsesDefault() {
        when(jobApplicationService.findApplicationOrThrow(42L, 7L)).thenReturn(application);
        saveReturnsArgument();

        NoteResponse response = noteService.addNote(user, 42L, new CreateNoteRequest("   ", "content"));

        assertThat(response.getTitle()).isEqualTo(NoteService.DEFAULT_NOTE_TITLE);
    }

    @Test
    void addNoteToForeignApplicationFails() {
        when(jobApplicationService.findApplicationOrThrow(99L, 7L))
                .thenThrow(new ResourceNotFoundException("Job application not found with ID: 99"));

        assertThatThrownBy(() -> noteService.addNote(user, 99L, new CreateNoteRequest(null, "content")))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(noteRepository, never()).save(any());
    }

    @Test
    void updateNoteWithBlankTitleKeepsExistingTitle() {
        Note existing = new Note(application, "Offer details", "old");
        when(noteRepository.findByIdAndJobApplicationUserId(5L, 7L)).thenReturn(Optional.of(existing));
        saveReturnsArgument();

        NoteResponse response = noteService.updateNote(user, 5L, new UpdateNoteRequest("  ", " new content "));

        assertThat(response.getTitle()).isEqualTo("Offer details");
        assertThat(response.getContent()).isEqualTo("new content");
    }

    @Test
    void updateNoteWithTitleReplacesIt() {
        Note existing = new Note(application, "Old", "old");
        when(noteRepository.findByIdAndJobApplicationUserId(5L, 7L)).thenReturn(Optional.of(existing));
        saveReturnsArgument();

        NoteResponse response = noteService.updateNote(user, 5L, new UpdateNoteRequest(" New ", "body"));

        assertThat(response.getTitle()).isEqualTo("New");
    }

    @Test
    void updateMissingNoteIsNotFound() {
        when(noteRepository.findByIdAndJobApplicationUserId(5L, 7L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> noteService.updateNote(user, 5L, new UpdateNoteRequest(null, "body")))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("5");
    }

    @Test
    void deleteNoteRemovesOwnedNote() {
        Note existing = new Note(application, "t", "c");
        when(noteRepository.findByIdAndJobApplicationUserId(5L, 7L)).thenReturn(Optional.of(existing));

        noteService.deleteNote(user, 5L);

        verify(noteRepository).delete(existing);
    }

    @Test
    void listNotesChecksOwnershipFirst() {
        when(jobApplicationService.findApplicationOrThrow(42L, 7L)).thenReturn(application);
        when(noteRepository.findByJobApplicationIdAndJobApplicationUserIdOrderByCreatedAtDesc(42L, 7L))
                .thenReturn(List.of(new Note(application, "a", "1"), new Note(application, "b", "2")));

        assertThat(noteService.getNotesByApplication(user, 42L))
                .extracting(NoteResponse::getTitle)
                .containsExactly("a", "b");
    }
}
