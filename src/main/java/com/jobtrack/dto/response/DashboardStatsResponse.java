package com.jobtrack.dto.response;

import com.jobtrack.entity.enums.ApplicationStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DashboardStatsResponse {

    private long totalApplications;
    private long activeApplications;
    private long totalInterviews;
    private long upcomingInterviewsCount;
    private double responseRatePercentage;
    private double offerRatePercentage;
    private Map<ApplicationStatus, Long> statusCounts = new HashMap<>();
    private List<InterviewResponse> upcomingInterviews = new ArrayList<>();

    public DashboardStatsResponse() {
    }

    public long getTotalApplications() {
        return totalApplications;
    }

    public void setTotalApplications(long totalApplications) {
        this.totalApplications = totalApplications;
    }

    public long getActiveApplications() {
        return activeApplications;
    }

    public void setActiveApplications(long activeApplications) {
        this.activeApplications = activeApplications;
    }

    public long getTotalInterviews() {
        return totalInterviews;
    }

    public void setTotalInterviews(long totalInterviews) {
        this.totalInterviews = totalInterviews;
    }

    public long getUpcomingInterviewsCount() {
        return upcomingInterviewsCount;
    }

    public void setUpcomingInterviewsCount(long upcomingInterviewsCount) {
        this.upcomingInterviewsCount = upcomingInterviewsCount;
    }

    public double getResponseRatePercentage() {
        return responseRatePercentage;
    }

    public void setResponseRatePercentage(double responseRatePercentage) {
        this.responseRatePercentage = responseRatePercentage;
    }

    public double getOfferRatePercentage() {
        return offerRatePercentage;
    }

    public void setOfferRatePercentage(double offerRatePercentage) {
        this.offerRatePercentage = offerRatePercentage;
    }

    public Map<ApplicationStatus, Long> getStatusCounts() {
        return statusCounts;
    }

    public void setStatusCounts(Map<ApplicationStatus, Long> statusCounts) {
        this.statusCounts = statusCounts;
    }

    public List<InterviewResponse> getUpcomingInterviews() {
        return upcomingInterviews;
    }

    public void setUpcomingInterviews(List<InterviewResponse> upcomingInterviews) {
        this.upcomingInterviews = upcomingInterviews;
    }
}
