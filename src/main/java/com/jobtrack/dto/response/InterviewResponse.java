package com.jobtrack.dto.response;

import com.jobtrack.entity.Interview;
import com.jobtrack.entity.enums.InterviewStatus;
import com.jobtrack.entity.enums.InterviewType;

import java.time.LocalDateTime;

public class InterviewResponse {

    private Long id;
    private Long applicationId;
    private String roundName;
    private InterviewType interviewType;
    private LocalDateTime scheduledAt;
    private String interviewerName;
    private String interviewerEmail;
    private String locationOrLink;
    private InterviewStatus status;
    private String feedback;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public InterviewResponse() {
    }

    public static InterviewResponse fromEntity(Interview interview) {
        if (interview == null) return null;
        InterviewResponse response = new InterviewResponse();
        response.setId(interview.getId());
        if (interview.getJobApplication() != null) {
            response.setApplicationId(interview.getJobApplication().getId());
        }
        response.setRoundName(interview.getRoundName());
        response.setInterviewType(interview.getInterviewType());
        response.setScheduledAt(interview.getScheduledAt());
        response.setInterviewerName(interview.getInterviewerName());
        response.setInterviewerEmail(interview.getInterviewerEmail());
        response.setLocationOrLink(interview.getLocationOrLink());
        response.setStatus(interview.getStatus());
        response.setFeedback(interview.getFeedback());
        response.setNotes(interview.getNotes());
        response.setCreatedAt(interview.getCreatedAt());
        response.setUpdatedAt(interview.getUpdatedAt());
        return response;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(Long applicationId) {
        this.applicationId = applicationId;
    }

    public String getRoundName() {
        return roundName;
    }

    public void setRoundName(String roundName) {
        this.roundName = roundName;
    }

    public InterviewType getInterviewType() {
        return interviewType;
    }

    public void setInterviewType(InterviewType interviewType) {
        this.interviewType = interviewType;
    }

    public LocalDateTime getScheduledAt() {
        return scheduledAt;
    }

    public void setScheduledAt(LocalDateTime scheduledAt) {
        this.scheduledAt = scheduledAt;
    }

    public String getInterviewerName() {
        return interviewerName;
    }

    public void setInterviewerName(String interviewerName) {
        this.interviewerName = interviewerName;
    }

    public String getInterviewerEmail() {
        return interviewerEmail;
    }

    public void setInterviewerEmail(String interviewerEmail) {
        this.interviewerEmail = interviewerEmail;
    }

    public String getLocationOrLink() {
        return locationOrLink;
    }

    public void setLocationOrLink(String locationOrLink) {
        this.locationOrLink = locationOrLink;
    }

    public InterviewStatus getStatus() {
        return status;
    }

    public void setStatus(InterviewStatus status) {
        this.status = status;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
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
