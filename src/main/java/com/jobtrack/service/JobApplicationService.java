package com.jobtrack.service;

import com.jobtrack.dto.request.CreateJobApplicationRequest;
import com.jobtrack.dto.request.UpdateJobApplicationRequest;
import com.jobtrack.dto.request.UpdateStatusRequest;
import com.jobtrack.dto.response.JobApplicationResponse;
import com.jobtrack.dto.response.PagedResponse;
import com.jobtrack.entity.JobApplication;
import com.jobtrack.entity.Note;
import com.jobtrack.entity.User;
import com.jobtrack.entity.enums.ApplicationStatus;
import com.jobtrack.entity.enums.EmploymentType;
import com.jobtrack.exception.ResourceNotFoundException;
import com.jobtrack.repository.JobApplicationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class JobApplicationService {

    private final JobApplicationRepository jobApplicationRepository;
    private final CsvExportService csvExportService;

    public JobApplicationService(JobApplicationRepository jobApplicationRepository,
                                 CsvExportService csvExportService) {
        this.jobApplicationRepository = jobApplicationRepository;
        this.csvExportService = csvExportService;
    }

    @Transactional
    public JobApplicationResponse createApplication(User user, CreateJobApplicationRequest request) {
        JobApplication application = new JobApplication();
        application.setUser(user);
        application.setCompanyName(request.getCompanyName().trim());
        application.setJobTitle(request.getJobTitle().trim());
        application.setJobUrl(request.getJobUrl());
        application.setLocation(request.getLocation());
        application.setEmploymentType(request.getEmploymentType() != null ? request.getEmploymentType() : EmploymentType.FULL_TIME);
        application.setMinSalary(request.getMinSalary());
        application.setMaxSalary(request.getMaxSalary());
        application.setSalaryCurrency(request.getSalaryCurrency() != null ? request.getSalaryCurrency() : "USD");
        application.setStatus(request.getStatus() != null ? request.getStatus() : ApplicationStatus.APPLIED);
        application.setAppliedDate(request.getAppliedDate() != null ? request.getAppliedDate() : LocalDate.now());
        application.setDeadline(request.getDeadline());

        if (request.getInitialNotes() != null && !request.getInitialNotes().trim().isEmpty()) {
            Note initialNote = new Note(application, "Initial Note", request.getInitialNotes().trim());
            application.addNote(initialNote);
        }

        JobApplication saved = jobApplicationRepository.save(application);
        return JobApplicationResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public JobApplicationResponse getApplicationById(User user, Long applicationId) {
        JobApplication application = findApplicationOrThrow(applicationId, user.getId());
        return JobApplicationResponse.fromEntity(application);
    }

    @Transactional(readOnly = true)
    public PagedResponse<JobApplicationResponse> getApplications(
            User user,
            ApplicationStatus status,
            EmploymentType employmentType,
            String search,
            Pageable pageable
    ) {
        Page<JobApplication> page = jobApplicationRepository.searchApplications(
                user.getId(),
                status,
                employmentType,
                search,
                pageable
        );

        Page<JobApplicationResponse> responsePage = page.map(JobApplicationResponse::fromEntity);
        return PagedResponse.of(responsePage);
    }

    @Transactional
    public JobApplicationResponse updateApplication(User user, Long applicationId, UpdateJobApplicationRequest request) {
        JobApplication application = findApplicationOrThrow(applicationId, user.getId());

        application.setCompanyName(request.getCompanyName().trim());
        application.setJobTitle(request.getJobTitle().trim());
        application.setJobUrl(request.getJobUrl());
        application.setLocation(request.getLocation());
        if (request.getEmploymentType() != null) {
            application.setEmploymentType(request.getEmploymentType());
        }
        application.setMinSalary(request.getMinSalary());
        application.setMaxSalary(request.getMaxSalary());
        if (request.getSalaryCurrency() != null) {
            application.setSalaryCurrency(request.getSalaryCurrency());
        }
        if (request.getStatus() != null) {
            application.setStatus(request.getStatus());
        }
        if (request.getAppliedDate() != null) {
            application.setAppliedDate(request.getAppliedDate());
        }
        application.setDeadline(request.getDeadline());

        JobApplication updated = jobApplicationRepository.save(application);
        return JobApplicationResponse.fromEntity(updated);
    }

    @Transactional
    public JobApplicationResponse updateStatus(User user, Long applicationId, UpdateStatusRequest request) {
        JobApplication application = findApplicationOrThrow(applicationId, user.getId());
        ApplicationStatus previousStatus = application.getStatus();
        application.setStatus(request.getStatus());

        if (request.getOptionalNote() != null && !request.getOptionalNote().trim().isEmpty()) {
            Note note = new Note(
                    application,
                    "Status changed from " + previousStatus + " to " + request.getStatus(),
                    request.getOptionalNote().trim()
            );
            application.addNote(note);
        }

        JobApplication updated = jobApplicationRepository.save(application);
        return JobApplicationResponse.fromEntity(updated);
    }

    @Transactional
    public void deleteApplication(User user, Long applicationId) {
        JobApplication application = findApplicationOrThrow(applicationId, user.getId());
        jobApplicationRepository.delete(application);
    }

    @Transactional(readOnly = true)
    public byte[] exportApplicationsCsv(User user, ApplicationStatus status) {
        java.util.List<JobApplication> applications = jobApplicationRepository.findAllForExport(user.getId(), status);
        return csvExportService.generateApplicationsCsv(applications);
    }

    public JobApplication findApplicationOrThrow(Long applicationId, Long userId) {
        return jobApplicationRepository.findByIdAndUserId(applicationId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Job application not found with ID: " + applicationId));
    }
}
