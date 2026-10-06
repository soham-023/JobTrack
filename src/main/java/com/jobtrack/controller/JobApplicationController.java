package com.jobtrack.controller;

import com.jobtrack.dto.request.CreateJobApplicationRequest;
import com.jobtrack.dto.request.UpdateJobApplicationRequest;
import com.jobtrack.dto.request.UpdateStatusRequest;
import com.jobtrack.dto.response.JobApplicationResponse;
import com.jobtrack.dto.response.PagedResponse;
import com.jobtrack.entity.User;
import com.jobtrack.entity.enums.ApplicationStatus;
import com.jobtrack.entity.enums.EmploymentType;
import com.jobtrack.service.AuthService;
import com.jobtrack.service.JobApplicationService;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/applications")
public class JobApplicationController {

    private final JobApplicationService jobApplicationService;
    private final AuthService authService;

    public JobApplicationController(JobApplicationService jobApplicationService, AuthService authService) {
        this.jobApplicationService = jobApplicationService;
        this.authService = authService;
    }

    @PostMapping
    public ResponseEntity<JobApplicationResponse> createApplication(
            @Valid @RequestBody CreateJobApplicationRequest request
    ) {
        User currentUser = authService.getCurrentAuthenticatedUser();
        JobApplicationResponse response = jobApplicationService.createApplication(currentUser, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<PagedResponse<JobApplicationResponse>> getApplications(
            @RequestParam(required = false) ApplicationStatus status,
            @RequestParam(required = false) EmploymentType employmentType,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        User currentUser = authService.getCurrentAuthenticatedUser();
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        PagedResponse<JobApplicationResponse> response = jobApplicationService.getApplications(
                currentUser, status, employmentType, search, pageable
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<JobApplicationResponse> getApplicationById(@PathVariable Long id) {
        User currentUser = authService.getCurrentAuthenticatedUser();
        JobApplicationResponse response = jobApplicationService.getApplicationById(currentUser, id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<JobApplicationResponse> updateApplication(
            @PathVariable Long id,
            @Valid @RequestBody UpdateJobApplicationRequest request
    ) {
        User currentUser = authService.getCurrentAuthenticatedUser();
        JobApplicationResponse response = jobApplicationService.updateApplication(currentUser, id, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<JobApplicationResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateStatusRequest request
    ) {
        User currentUser = authService.getCurrentAuthenticatedUser();
        JobApplicationResponse response = jobApplicationService.updateStatus(currentUser, id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteApplication(@PathVariable Long id) {
        User currentUser = authService.getCurrentAuthenticatedUser();
        jobApplicationService.deleteApplication(currentUser, id);
        return ResponseEntity.noContent().build();
    }
}
