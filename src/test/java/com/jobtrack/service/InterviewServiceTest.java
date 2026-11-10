package com.jobtrack.service;

import com.jobtrack.dto.request.CreateInterviewRequest;
import com.jobtrack.dto.response.InterviewResponse;
import com.jobtrack.entity.Interview;
import com.jobtrack.entity.JobApplication;
import com.jobtrack.entity.User;
import com.jobtrack.entity.enums.ApplicationStatus;
import com.jobtrack.entity.enums.InterviewStatus;
import com.jobtrack.entity.enums.InterviewType;
import com.jobtrack.entity.enums.Role;
import com.jobtrack.exception.ResourceNotFoundException;
import com.jobtrack.repository.InterviewRepository;
import com.jobtrack.repository.JobApplicationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InterviewServiceTest {

    @Mock
    private InterviewRepository interviewRepository;

    @Mock
    private JobApplicationService jobApplicationService;

    @Mock
    private JobApplicationRepository jobApplicationRepository;

    @InjectMocks
    private InterviewService interviewService;

    private User user;
    private JobApplication application;

    @BeforeEach
    void setUp() {
        user = new User("candidate@example.com", "pass", "Candidate", Role.ROLE_USER);
        user.setId(3L);
        application = new JobApplication();
        application.setId(11L);
        application.setUser(user);
        lenient().when(jobApplicationService.findApplicationOrThrow(11L, 3L)).thenReturn(application);
        lenient().when(interviewRepository.save(any(Interview.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    private static CreateInterviewRequest request(String roundName) {
        CreateInterviewRequest request = new CreateInterviewRequest();
        request.setRoundName(roundName);
        request.setScheduledAt(LocalDateTime.now().plusDays(2));
        return request;
    }

    @Test
    void addInterviewAppliesDefaultsAndTrimsRoundName() {
        application.setStatus(ApplicationStatus.OFFER);

        InterviewResponse response = interviewService.addInterview(user, 11L, request("  System Design  "));

        assertThat(response.getRoundName()).isEqualTo("System Design");
        assertThat(response.getInterviewType()).isEqualTo(InterviewType.TECHNICAL);
        assertThat(response.getStatus()).isEqualTo(InterviewStatus.SCHEDULED);
    }

    @ParameterizedTest
    @EnumSource(value = ApplicationStatus.class, names = {"WISHLIST", "APPLIED", "SCREENING"})
    void earlyStageApplicationsArePromotedToInterviewing(ApplicationStatus initial) {
        application.setStatus(initial);

        interviewService.addInterview(user, 11L, request("Phone screen"));

        assertThat(application.getStatus()).isEqualTo(ApplicationStatus.INTERVIEWING);
        verify(jobApplicationRepository).save(application);
    }

    @ParameterizedTest
    @EnumSource(value = ApplicationStatus.class, names = {"INTERVIEWING", "OFFER", "REJECTED", "ACCEPTED", "WITHDRAWN"})
    void laterStageApplicationsKeepTheirStatus(ApplicationStatus initial) {
        application.setStatus(initial);

        interviewService.addInterview(user, 11L, request("Follow-up"));

        assertThat(application.getStatus()).isEqualTo(initial);
        verify(jobApplicationRepository, never()).save(any());
    }

    @Test
    void addInterviewToForeignApplicationFails() {
        when(jobApplicationService.findApplicationOrThrow(99L, 3L))
                .thenThrow(new ResourceNotFoundException("Job application not found with ID: 99"));

        assertThatThrownBy(() -> interviewService.addInterview(user, 99L, request("Onsite")))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(interviewRepository, never()).save(any());
    }

    @Test
    void missingInterviewIsNotFound() {
        when(interviewRepository.findByIdAndJobApplicationUserId(8L, 3L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> interviewService.getInterviewById(user, 8L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("8");
    }

    @Test
    void deleteInterviewRemovesOwnedInterview() {
        Interview interview = new Interview();
        interview.setJobApplication(application);
        when(interviewRepository.findByIdAndJobApplicationUserId(8L, 3L)).thenReturn(Optional.of(interview));

        interviewService.deleteInterview(user, 8L);

        verify(interviewRepository).delete(interview);
    }
}
