package com.jobtrack.controller;

import com.jobtrack.dto.request.CreateInterviewRequest;
import com.jobtrack.dto.request.UpdateInterviewRequest;
import com.jobtrack.dto.response.InterviewResponse;
import com.jobtrack.entity.User;
import com.jobtrack.service.AuthService;
import com.jobtrack.service.InterviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "3. Interview Rounds", description = "Schedule and manage interview rounds, links, and auto-lifecycle transitions")
public class InterviewController {

    private final InterviewService interviewService;
    private final AuthService authService;

    public InterviewController(InterviewService interviewService, AuthService authService) {
        this.interviewService = interviewService;
        this.authService = authService;
    }

    @PostMapping("/applications/{applicationId}/interviews")
    @Operation(summary = "Schedule an interview round", description = "Schedules round, interviewer details, and video link. Automatically promotes application status to INTERVIEWING.")
    public ResponseEntity<InterviewResponse> addInterview(
            @PathVariable Long applicationId,
            @Valid @RequestBody CreateInterviewRequest request
    ) {
        User currentUser = authService.getCurrentAuthenticatedUser();
        InterviewResponse response = interviewService.addInterview(currentUser, applicationId, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/applications/{applicationId}/interviews")
    @Operation(summary = "Get all interviews for an application", description = "Retrieves chronological list of interview rounds for the specified application.")
    public ResponseEntity<List<InterviewResponse>> getInterviewsByApplication(@PathVariable Long applicationId) {
        User currentUser = authService.getCurrentAuthenticatedUser();
        List<InterviewResponse> interviews = interviewService.getInterviewsByApplication(currentUser, applicationId);
        return ResponseEntity.ok(interviews);
    }

    @GetMapping("/interviews/{id}")
    @Operation(summary = "Get interview by ID", description = "Returns interview details, type, feedback, and notes.")
    public ResponseEntity<InterviewResponse> getInterviewById(@PathVariable Long id) {
        User currentUser = authService.getCurrentAuthenticatedUser();
        InterviewResponse response = interviewService.getInterviewById(currentUser, id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/interviews/{id}")
    @Operation(summary = "Update interview round", description = "Updates interview schedule, interviewer info, feedback, and status.")
    public ResponseEntity<InterviewResponse> updateInterview(
            @PathVariable Long id,
            @Valid @RequestBody UpdateInterviewRequest request
    ) {
        User currentUser = authService.getCurrentAuthenticatedUser();
        InterviewResponse response = interviewService.updateInterview(currentUser, id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/interviews/{id}")
    @Operation(summary = "Delete interview round", description = "Removes interview round record.")
    public ResponseEntity<Void> deleteInterview(@PathVariable Long id) {
        User currentUser = authService.getCurrentAuthenticatedUser();
        interviewService.deleteInterview(currentUser, id);
        return ResponseEntity.noContent().build();
    }
}
