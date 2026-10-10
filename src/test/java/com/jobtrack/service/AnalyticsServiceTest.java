package com.jobtrack.service;

import com.jobtrack.dto.response.DashboardStatsResponse;
import com.jobtrack.entity.User;
import com.jobtrack.entity.enums.ApplicationStatus;
import com.jobtrack.entity.enums.Role;
import com.jobtrack.repository.InterviewRepository;
import com.jobtrack.repository.JobApplicationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {
    

    @Mock
    private JobApplicationRepository jobApplicationRepository;

    @Mock
    private InterviewRepository interviewRepository;

    @InjectMocks
    private AnalyticsService analyticsService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User("tester@example.com", "pass", "Tester", Role.ROLE_USER);
        testUser.setId(10L);
    }

    @Test
    @DisplayName("Should correctly calculate analytics rates and status breakdown")
    void testGetDashboardStats() {
        when(jobApplicationRepository.countByUserId(10L)).thenReturn(4L);

        List<Object[]> statusCounts = new ArrayList<>();
        statusCounts.add(new Object[]{ApplicationStatus.APPLIED, 1L});
        statusCounts.add(new Object[]{ApplicationStatus.INTERVIEWING, 1L});
        statusCounts.add(new Object[]{ApplicationStatus.OFFER, 1L});
        statusCounts.add(new Object[]{ApplicationStatus.REJECTED, 1L});

        when(jobApplicationRepository.countByStatusForUser(10L)).thenReturn(statusCounts);
        when(interviewRepository.countByJobApplicationUserId(10L)).thenReturn(2L);
        when(interviewRepository.countByJobApplicationUserIdAndScheduledAtAfterAndStatus(eq(10L), any(), any())).thenReturn(1L);
        when(interviewRepository.findByJobApplicationUserIdAndScheduledAtAfterOrderByScheduledAtAsc(eq(10L), any())).thenReturn(List.of());

        DashboardStatsResponse stats = analyticsService.getDashboardStats(testUser);

        assertThat(stats.getTotalApplications()).isEqualTo(4L);
        assertThat(stats.getActiveApplications()).isEqualTo(3L); // APPLIED + INTERVIEWING + OFFER
        assertThat(stats.getTotalInterviews()).isEqualTo(2L);
        assertThat(stats.getUpcomingInterviewsCount()).isEqualTo(1L);
        // Response rate: (INTERVIEWING + OFFER) / 4 = 2 / 4 = 50.0%
        assertThat(stats.getResponseRatePercentage()).isEqualTo(50.0);
        // Offer rate: OFFER / 4 = 1 / 4 = 25.0%
        assertThat(stats.getOfferRatePercentage()).isEqualTo(25.0);
    }
}
