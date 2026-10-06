package com.jobtrack.controller;

import com.jobtrack.dto.request.CreateNoteRequest;
import com.jobtrack.dto.request.UpdateNoteRequest;
import com.jobtrack.dto.response.NoteResponse;
import com.jobtrack.entity.User;
import com.jobtrack.service.AuthService;
import com.jobtrack.service.NoteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class NoteController {

    private final NoteService noteService;
    private final AuthService authService;

    public NoteController(NoteService noteService, AuthService authService) {
        this.noteService = noteService;
        this.authService = authService;
    }

    @PostMapping("/applications/{applicationId}/notes")
    public ResponseEntity<NoteResponse> addNote(
            @PathVariable Long applicationId,
            @Valid @RequestBody CreateNoteRequest request
    ) {
        User currentUser = authService.getCurrentAuthenticatedUser();
        NoteResponse response = noteService.addNote(currentUser, applicationId, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/applications/{applicationId}/notes")
    public ResponseEntity<List<NoteResponse>> getNotesByApplication(@PathVariable Long applicationId) {
        User currentUser = authService.getCurrentAuthenticatedUser();
        List<NoteResponse> notes = noteService.getNotesByApplication(currentUser, applicationId);
        return ResponseEntity.ok(notes);
    }

    @PutMapping("/notes/{id}")
    public ResponseEntity<NoteResponse> updateNote(
            @PathVariable Long id,
            @Valid @RequestBody UpdateNoteRequest request
    ) {
        User currentUser = authService.getCurrentAuthenticatedUser();
        NoteResponse response = noteService.updateNote(currentUser, id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/notes/{id}")
    public ResponseEntity<Void> deleteNote(@PathVariable Long id) {
        User currentUser = authService.getCurrentAuthenticatedUser();
        noteService.deleteNote(currentUser, id);
        return ResponseEntity.noContent().build();
    }
}
