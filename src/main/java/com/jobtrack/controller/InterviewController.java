package com.jobtrack.controller;

import com.jobtrack.dto.request.CreateInterviewRequest;
import com.jobtrack.dto.request.UpdateInterviewRequest;
import com.jobtrack.dto.response.InterviewResponse;
import com.jobtrack.entity.User;
import com.jobtrack.service.AuthService;
import com.jobtrack.service.InterviewService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class InterviewController {

    private final InterviewService interviewService;
    private final AuthService authService;

    public InterviewController(InterviewService interviewService, AuthService authService) {
        this.interviewService = interviewService;
        this.authService = authService;
    }

    @PostMapping("/applications/{applicationId}/interviews")
    public ResponseEntity<InterviewResponse> addInterview(
            @PathVariable Long applicationId,
            @Valid @RequestBody CreateInterviewRequest request
    ) {
        User currentUser = authService.getCurrentAuthenticatedUser();
        InterviewResponse response = interviewService.addInterview(currentUser, applicationId, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/applications/{applicationId}/interviews")
    public ResponseEntity<List<InterviewResponse>> getInterviewsByApplication(@PathVariable Long applicationId) {
        User currentUser = authService.getCurrentAuthenticatedUser();
        List<InterviewResponse> interviews = interviewService.getInterviewsByApplication(currentUser, applicationId);
        return ResponseEntity.ok(interviews);
    }

    @GetMapping("/interviews/{id}")
    public ResponseEntity<InterviewResponse> getInterviewById(@PathVariable Long id) {
        User currentUser = authService.getCurrentAuthenticatedUser();
        InterviewResponse response = interviewService.getInterviewById(currentUser, id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/interviews/{id}")
    public ResponseEntity<InterviewResponse> updateInterview(
            @PathVariable Long id,
            @Valid @RequestBody UpdateInterviewRequest request
    ) {
        User currentUser = authService.getCurrentAuthenticatedUser();
        InterviewResponse response = interviewService.updateInterview(currentUser, id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/interviews/{id}")
    public ResponseEntity<Void> deleteInterview(@PathVariable Long id) {
        User currentUser = authService.getCurrentAuthenticatedUser();
        interviewService.deleteInterview(currentUser, id);
        return ResponseEntity.noContent().build();
    }
}
