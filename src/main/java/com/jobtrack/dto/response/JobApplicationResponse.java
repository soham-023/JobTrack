package com.jobtrack.dto.response;

import com.jobtrack.entity.JobApplication;
import com.jobtrack.entity.enums.ApplicationStatus;
import com.jobtrack.entity.enums.EmploymentType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class JobApplicationResponse {

    private Long id;
    private Long userId;
    private String companyName;
    private String jobTitle;
    private String jobUrl;
    private String location;
    private EmploymentType employmentType;
    private BigDecimal minSalary;
    private BigDecimal maxSalary;
    private String salaryCurrency;
    private ApplicationStatus status;
    private LocalDate appliedDate;
    private LocalDate deadline;
    private List<InterviewResponse> interviews = new ArrayList<>();
    private List<NoteResponse> notes = new ArrayList<>();
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public JobApplicationResponse() {
    }

    public static JobApplicationResponse fromEntity(JobApplication app) {
        if (app == null) return null;
        JobApplicationResponse response = new JobApplicationResponse();
        response.setId(app.getId());
        if (app.getUser() != null) {
            response.setUserId(app.getUser().getId());
        }
        response.setCompanyName(app.getCompanyName());
        response.setJobTitle(app.getJobTitle());
        response.setJobUrl(app.getJobUrl());
        response.setLocation(app.getLocation());
        response.setEmploymentType(app.getEmploymentType());
        response.setMinSalary(app.getMinSalary());
        response.setMaxSalary(app.getMaxSalary());
        response.setSalaryCurrency(app.getSalaryCurrency());
        response.setStatus(app.getStatus());
        response.setAppliedDate(app.getAppliedDate());
        response.setDeadline(app.getDeadline());

        if (app.getInterviews() != null) {
            response.setInterviews(app.getInterviews().stream()
                    .map(InterviewResponse::fromEntity)
                    .collect(Collectors.toList()));
        }

        if (app.getNotes() != null) {
            response.setNotes(app.getNotes().stream()
                    .map(NoteResponse::fromEntity)
                    .collect(Collectors.toList()));
        }

        response.setCreatedAt(app.getCreatedAt());
        response.setUpdatedAt(app.getUpdatedAt());
        return response;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public String getJobUrl() {
        return jobUrl;
    }

    public void setJobUrl(String jobUrl) {
        this.jobUrl = jobUrl;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public EmploymentType getEmploymentType() {
        return employmentType;
    }

    public void setEmploymentType(EmploymentType employmentType) {
        this.employmentType = employmentType;
    }

    public BigDecimal getMinSalary() {
        return minSalary;
    }

    public void setMinSalary(BigDecimal minSalary) {
        this.minSalary = minSalary;
    }

    public BigDecimal getMaxSalary() {
        return maxSalary;
    }

    public void setMaxSalary(BigDecimal maxSalary) {
        this.maxSalary = maxSalary;
    }

    public String getSalaryCurrency() {
        return salaryCurrency;
    }

    public void setSalaryCurrency(String salaryCurrency) {
        this.salaryCurrency = salaryCurrency;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public void setStatus(ApplicationStatus status) {
        this.status = status;
    }

    public LocalDate getAppliedDate() {
        return appliedDate;
    }

    public void setAppliedDate(LocalDate appliedDate) {
        this.appliedDate = appliedDate;
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDate deadline) {
        this.deadline = deadline;
    }

    public List<InterviewResponse> getInterviews() {
        return interviews;
    }

    public void setInterviews(List<InterviewResponse> interviews) {
        this.interviews = interviews;
    }

    public List<NoteResponse> getNotes() {
        return notes;
    }

    public void setNotes(List<NoteResponse> notes) {
        this.notes = notes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
