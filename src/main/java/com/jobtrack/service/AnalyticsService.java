package com.jobtrack.service;

import com.jobtrack.dto.response.DashboardStatsResponse;
import com.jobtrack.dto.response.InterviewResponse;
import com.jobtrack.entity.User;
import com.jobtrack.entity.enums.ApplicationStatus;
import com.jobtrack.entity.enums.InterviewStatus;
import com.jobtrack.repository.InterviewRepository;
import com.jobtrack.repository.JobApplicationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {

    private final JobApplicationRepository jobApplicationRepository;
    private final InterviewRepository interviewRepository;

    public AnalyticsService(JobApplicationRepository jobApplicationRepository,
                            InterviewRepository interviewRepository) {
        this.jobApplicationRepository = jobApplicationRepository;
        this.interviewRepository = interviewRepository;
    }

    @Transactional(readOnly = true)
    public DashboardStatsResponse getDashboardStats(User user) {
        DashboardStatsResponse stats = new DashboardStatsResponse();

        long totalApps = jobApplicationRepository.countByUserId(user.getId());
        stats.setTotalApplications(totalApps);

        // Pre-populate all statuses with count 0
        Map<ApplicationStatus, Long> statusCounts = new EnumMap<>(ApplicationStatus.class);
        for (ApplicationStatus status : ApplicationStatus.values()) {
            statusCounts.put(status, 0L);
        }

        List<Object[]> rawCounts = jobApplicationRepository.countByStatusForUser(user.getId());
        for (Object[] row : rawCounts) {
            ApplicationStatus status = (ApplicationStatus) row[0];
            Long count = (Long) row[1];
            statusCounts.put(status, count);
        }
        stats.setStatusCounts(statusCounts);

        // Active applications: not REJECTED, ACCEPTED, or WITHDRAWN
        long activeCount = statusCounts.entrySet().stream()
                .filter(entry -> entry.getKey() != ApplicationStatus.REJECTED &&
                                 entry.getKey() != ApplicationStatus.ACCEPTED &&
                                 entry.getKey() != ApplicationStatus.WITHDRAWN)
                .mapToLong(Map.Entry::getValue)
                .sum();
        stats.setActiveApplications(activeCount);

        // Interview metrics
        long totalInterviews = interviewRepository.countByJobApplicationUserId(user.getId());
        stats.setTotalInterviews(totalInterviews);

        LocalDateTime now = LocalDateTime.now();
        long upcomingCount = interviewRepository.countByJobApplicationUserIdAndScheduledAtAfterAndStatus(
                user.getId(), now, InterviewStatus.SCHEDULED);
        stats.setUpcomingInterviewsCount(upcomingCount);

        List<InterviewResponse> upcomingInterviews = interviewRepository
                .findByJobApplicationUserIdAndScheduledAtAfterOrderByScheduledAtAsc(user.getId(), now)
                .stream()
                .filter(i -> i.getStatus() == InterviewStatus.SCHEDULED)
                .limit(5)
                .map(InterviewResponse::fromEntity)
                .collect(Collectors.toList());
        stats.setUpcomingInterviews(upcomingInterviews);

        // Rates
        if (totalApps > 0) {
            long respondedCount = statusCounts.getOrDefault(ApplicationStatus.SCREENING, 0L)
                    + statusCounts.getOrDefault(ApplicationStatus.INTERVIEWING, 0L)
                    + statusCounts.getOrDefault(ApplicationStatus.OFFER, 0L)
                    + statusCounts.getOrDefault(ApplicationStatus.ACCEPTED, 0L);
            double responseRate = (respondedCount * 100.0) / totalApps;
            stats.setResponseRatePercentage(Math.round(responseRate * 10.0) / 10.0);

            long offerCount = statusCounts.getOrDefault(ApplicationStatus.OFFER, 0L)
                    + statusCounts.getOrDefault(ApplicationStatus.ACCEPTED, 0L);
            double offerRate = (offerCount * 100.0) / totalApps;
            stats.setOfferRatePercentage(Math.round(offerRate * 10.0) / 10.0);
        } else {
            stats.setResponseRatePercentage(0.0);
            stats.setOfferRatePercentage(0.0);
        }

        return stats;
    }
}
