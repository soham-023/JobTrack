package com.jobtrack.dto.response;

import com.jobtrack.entity.Notification;

import java.time.LocalDateTime;

public class NotificationResponse {

    private Long id;
    private Long interviewId;
    private Long applicationId;
    private String companyName;
    private String title;
    private String message;
    private boolean isRead;
    private LocalDateTime createdAt;

    public NotificationResponse() {
    }

    public static NotificationResponse fromEntity(Notification notification) {
        if (notification == null) return null;
        NotificationResponse res = new NotificationResponse();
        res.setId(notification.getId());
        res.setTitle(notification.getTitle());
        res.setMessage(notification.getMessage());
        res.setRead(notification.isRead());
        res.setCreatedAt(notification.getCreatedAt());

        if (notification.getInterview() != null) {
            res.setInterviewId(notification.getInterview().getId());
            if (notification.getInterview().getJobApplication() != null) {
                res.setApplicationId(notification.getInterview().getJobApplication().getId());
                res.setCompanyName(notification.getInterview().getJobApplication().getCompanyName());
            }
        }
        return res;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getInterviewId() {
        return interviewId;
    }

    public void setInterviewId(Long interviewId) {
        this.interviewId = interviewId;
    }

    public Long getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(Long applicationId) {
        this.applicationId = applicationId;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
