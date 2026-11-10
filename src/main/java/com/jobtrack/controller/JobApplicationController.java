package com.jobtrack.controller;

import com.jobtrack.dto.request.CreateJobApplicationRequest;
import com.jobtrack.dto.request.UpdateJobApplicationRequest;
import com.jobtrack.dto.request.UpdateStatusRequest;
import com.jobtrack.dto.response.JobApplicationResponse;
import com.jobtrack.dto.response.PagedResponse;
import com.jobtrack.entity.User;
import com.jobtrack.entity.enums.ApplicationStatus;
import com.jobtrack.entity.enums.EmploymentType;
import com.jobtrack.exception.BadRequestException;
import com.jobtrack.service.AuthService;
import com.jobtrack.service.JobApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Set;
import java.util.TreeSet;

@RestController
@RequestMapping("/api/v1/applications")
@Tag(name = "2. Job Applications", description = "CRUD operations, lifecycle tracking, filtering, and RFC 4180 CSV export")
public class JobApplicationController {

    /** Largest page a client may request, to keep list queries bounded. */
    static final int MAX_PAGE_SIZE = 100;

    /** Entity properties that list results may be sorted by. */
    static final Set<String> SORTABLE_FIELDS = Set.of(
            "createdAt", "updatedAt", "appliedDate", "deadline", "companyName",
            "jobTitle", "location", "status", "employmentType", "minSalary", "maxSalary"
    );

    private final JobApplicationService jobApplicationService;
    private final AuthService authService;

    public JobApplicationController(JobApplicationService jobApplicationService, AuthService authService) {
        this.jobApplicationService = jobApplicationService;
        this.authService = authService;
    }

    @PostMapping
    @Operation(summary = "Create a new job application", description = "Stores job title, company, URLs, salary expectations, and initial notes.")
    public ResponseEntity<JobApplicationResponse> createApplication(
            @Valid @RequestBody CreateJobApplicationRequest request
    ) {
        User currentUser = authService.getCurrentAuthenticatedUser();
        JobApplicationResponse response = jobApplicationService.createApplication(currentUser, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "List job applications with filtering & pagination", description = "Filter by status and employment type, search keywords across company/title/location, and sort results.")
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
        Pageable pageable = buildPageable(page, size, sortBy, sortDir);

        PagedResponse<JobApplicationResponse> response = jobApplicationService.getApplications(
                currentUser, status, employmentType, search, pageable
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping(value = "/export", produces = "text/csv; charset=UTF-8")
    
    @Operation(summary = "Export applications as RFC 4180 CSV", description = "Generates a downloadable CSV spreadsheet with UTF-8 BOM, optionally filtered by status.")
    public ResponseEntity<byte[]> exportApplications(
        
            @RequestParam(required = false) ApplicationStatus status
        
    ) {
        User currentUser = authService.getCurrentAuthenticatedUser();
        byte[] csvData = jobApplicationService.exportApplicationsCsv(currentUser, status);
        
        String filename = "job_applications_" + LocalDate.now() + ".csv";
        

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .header(HttpHeaders.CONTENT_TYPE, "text/csv; charset=UTF-8")
                .body(csvData);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get application details by ID", description = "Returns full application record with attached interviews and notes.")
    public ResponseEntity<JobApplicationResponse> getApplicationById(@PathVariable Long id) {
        User currentUser = authService.getCurrentAuthenticatedUser();
        
        JobApplicationResponse response = jobApplicationService.getApplicationById(currentUser, id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update job application", description = "Updates job details, salary range, dates, and employment metadata.")
    public ResponseEntity<JobApplicationResponse> updateApplication(
            @PathVariable Long id,
            @Valid @RequestBody UpdateJobApplicationRequest request
    ) {
        User currentUser = authService.getCurrentAuthenticatedUser();
        JobApplicationResponse response = jobApplicationService.updateApplication(currentUser, id, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update application status lifecycle", description = "Promotes or updates status (e.g. APPLIED -> INTERVIEWING -> OFFER) and automatically creates an audit note.")
    public ResponseEntity<JobApplicationResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateStatusRequest request
    ) {
        User currentUser = authService.getCurrentAuthenticatedUser(); 
        
        JobApplicationResponse response = jobApplicationService.updateStatus(currentUser, id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete job application", description = "Permanently deletes an application and all associated rounds, notes, and documents.")
    public ResponseEntity<Void> deleteApplication(@PathVariable Long id) {
        User currentUser = authService.getCurrentAuthenticatedUser();
        
        jobApplicationService.deleteApplication(currentUser, id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Validates paging and sorting parameters and turns them into a {@link Pageable}.
     * Invalid input is reported as 400 Bad Request instead of surfacing as a
     * server error from Spring Data (e.g. an unknown sort property).
     */
    static Pageable buildPageable(int page, int size, String sortBy, String sortDir) {
        if (page < 0) {
            throw new BadRequestException("Page index must not be negative");
        }
        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new BadRequestException("Page size must be between 1 and " + MAX_PAGE_SIZE);
        }
        if (!SORTABLE_FIELDS.contains(sortBy)) {
            throw new BadRequestException("Cannot sort by '" + sortBy + "'. Allowed fields: " + new TreeSet<>(SORTABLE_FIELDS));
        }
        Sort.Direction direction;
        if ("asc".equalsIgnoreCase(sortDir)) {
            direction = Sort.Direction.ASC;
        } else if ("desc".equalsIgnoreCase(sortDir)) {
            direction = Sort.Direction.DESC;
        } else {
            throw new BadRequestException("Sort direction must be 'asc' or 'desc'");
        }
        return PageRequest.of(page, size, Sort.by(direction, sortBy));
    }
}
