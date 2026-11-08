package com.jobtrack.service;

import com.jobtrack.dto.request.CreateJobApplicationRequest;
import com.jobtrack.dto.request.UpdateStatusRequest;
import com.jobtrack.dto.response.JobApplicationResponse;
import com.jobtrack.entity.JobApplication;
import com.jobtrack.entity.User;
import com.jobtrack.entity.enums.ApplicationStatus;
import com.jobtrack.entity.enums.EmploymentType;
import com.jobtrack.entity.enums.Role;
import com.jobtrack.exception.BadRequestException;
import com.jobtrack.exception.ResourceNotFoundException;
import com.jobtrack.repository.JobApplicationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JobApplicationServiceTest {

    @Mock
    private JobApplicationRepository jobApplicationRepository;

    @Mock
    private CsvExportService csvExportService;

    @InjectMocks
    private JobApplicationService jobApplicationService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User("tester@example.com", "pass", "Tester", Role.ROLE_USER);
        user.setId(1L);
    }

    @Test
    @DisplayName("Creating an application trims names and applies sensible defaults")
    void createApplicationAppliesDefaults() {
        CreateJobApplicationRequest request = new CreateJobApplicationRequest();
        request.setCompanyName("  Acme  ");
        request.setJobTitle("  Backend Engineer ");
        request.setEmploymentType(null);
        request.setSalaryCurrency(null);
        request.setStatus(null);
        when(jobApplicationRepository.save(any(JobApplication.class))).thenAnswer(inv -> inv.getArgument(0));

        jobApplicationService.createApplication(user, request);

        ArgumentCaptor<JobApplication> captor = ArgumentCaptor.forClass(JobApplication.class);
        verify(jobApplicationRepository).save(captor.capture());
        JobApplication saved = captor.getValue();
        assertThat(saved.getCompanyName()).isEqualTo("Acme");
        assertThat(saved.getJobTitle()).isEqualTo("Backend Engineer");
        assertThat(saved.getEmploymentType()).isEqualTo(EmploymentType.FULL_TIME);
        assertThat(saved.getSalaryCurrency()).isEqualTo("USD");
        assertThat(saved.getStatus()).isEqualTo(ApplicationStatus.APPLIED);
        assertThat(saved.getAppliedDate()).isEqualTo(LocalDate.now());
    }

    @Test
    @DisplayName("Creating an application with min salary above max salary is rejected")
    void createApplicationRejectsInvertedSalaryRange() {
        CreateJobApplicationRequest request = new CreateJobApplicationRequest();
        request.setCompanyName("Acme");
        request.setJobTitle("Engineer");
        request.setMinSalary(BigDecimal.valueOf(200000));
        request.setMaxSalary(BigDecimal.valueOf(150000));

        assertThatThrownBy(() -> jobApplicationService.createApplication(user, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Minimum salary");
        verify(jobApplicationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Salary range validation allows open-ended and equal bounds")
    void validateSalaryRangeAllowsValidInputs() {
        assertThatCode(() -> JobApplicationService.validateSalaryRange(null, null)).doesNotThrowAnyException();
        assertThatCode(() -> JobApplicationService.validateSalaryRange(BigDecimal.TEN, null)).doesNotThrowAnyException();
        assertThatCode(() -> JobApplicationService.validateSalaryRange(null, BigDecimal.TEN)).doesNotThrowAnyException();
        assertThatCode(() -> JobApplicationService.validateSalaryRange(BigDecimal.TEN, BigDecimal.TEN)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Looking up another user's application throws ResourceNotFoundException")
    void getApplicationByIdNotFound() {
        when(jobApplicationRepository.findByIdAndUserId(99L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> jobApplicationService.getApplicationById(user, 99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("Status update records a note describing the transition")
    void updateStatusAddsTransitionNote() {
        JobApplication application = new JobApplication();
        application.setUser(user);
        application.setCompanyName("Acme");
        application.setJobTitle("Engineer");
        application.setStatus(ApplicationStatus.APPLIED);
        when(jobApplicationRepository.findByIdAndUserId(5L, 1L)).thenReturn(Optional.of(application));
        when(jobApplicationRepository.save(any(JobApplication.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateStatusRequest request = new UpdateStatusRequest();
        request.setStatus(ApplicationStatus.INTERVIEWING);
        request.setOptionalNote("Recruiter scheduled round 1");

        JobApplicationResponse response = jobApplicationService.updateStatus(user, 5L, request);

        assertThat(response.getStatus()).isEqualTo(ApplicationStatus.INTERVIEWING);
        assertThat(application.getNotes()).hasSize(1);
        assertThat(application.getNotes().get(0).getTitle())
                .isEqualTo("Status changed from APPLIED to INTERVIEWING");
    }
}
