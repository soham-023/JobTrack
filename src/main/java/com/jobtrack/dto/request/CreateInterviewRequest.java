package com.jobtrack.dto.request;

import com.jobtrack.entity.enums.InterviewStatus;
import com.jobtrack.entity.enums.InterviewType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public class CreateInterviewRequest {

    @NotBlank(message = "Round name is required (e.g. Recruiter Screen, Technical Round 1)")
    @Size(max = 100, message = "Round name must not exceed 100 characters")
    private String roundName;

    private InterviewType interviewType = InterviewType.TECHNICAL;

    @NotNull(message = "Scheduled time is required")
    private LocalDateTime scheduledAt;

    @Size(max = 100, message = "Interviewer name must not exceed 100 characters")
    private String interviewerName;

    @Size(max = 120, message = "Interviewer email must not exceed 120 characters")
    private String interviewerEmail;

    @Size(max = 500, message = "Location/meeting link must not exceed 500 characters")
    private String locationOrLink;

    private InterviewStatus status = InterviewStatus.SCHEDULED;

    private String feedback;

    private String notes;

    public CreateInterviewRequest() {
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
}
