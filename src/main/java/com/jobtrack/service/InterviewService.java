package com.jobtrack.service;

import com.jobtrack.dto.request.CreateInterviewRequest;
import com.jobtrack.dto.request.UpdateInterviewRequest;
import com.jobtrack.dto.response.InterviewResponse;
import com.jobtrack.entity.Interview;
import com.jobtrack.entity.JobApplication;
import com.jobtrack.entity.User;
import com.jobtrack.entity.enums.ApplicationStatus;
import com.jobtrack.entity.enums.InterviewStatus;
import com.jobtrack.entity.enums.InterviewType;
import com.jobtrack.exception.ResourceNotFoundException;
import com.jobtrack.repository.InterviewRepository;
import com.jobtrack.repository.JobApplicationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class InterviewService {

    private final InterviewRepository interviewRepository;
    private final JobApplicationService jobApplicationService;
    private final JobApplicationRepository jobApplicationRepository;

    public InterviewService(InterviewRepository interviewRepository,
                            JobApplicationService jobApplicationService,
                            JobApplicationRepository jobApplicationRepository) {
        this.interviewRepository = interviewRepository;
        this.jobApplicationService = jobApplicationService;
        this.jobApplicationRepository = jobApplicationRepository;
    }

    @Transactional
    public InterviewResponse addInterview(User user, Long applicationId, CreateInterviewRequest request) {
        JobApplication application = jobApplicationService.findApplicationOrThrow(applicationId, user.getId());

        Interview interview = new Interview();
        interview.setJobApplication(application);
        interview.setRoundName(request.getRoundName().trim());
        interview.setInterviewType(request.getInterviewType() != null ? request.getInterviewType() : InterviewType.TECHNICAL);
        interview.setScheduledAt(request.getScheduledAt());
        interview.setInterviewerName(request.getInterviewerName());
        interview.setInterviewerEmail(request.getInterviewerEmail());
        interview.setLocationOrLink(request.getLocationOrLink());
        interview.setStatus(request.getStatus() != null ? request.getStatus() : InterviewStatus.SCHEDULED);
        interview.setFeedback(request.getFeedback());
        interview.setNotes(request.getNotes());

        // Smart workflow: when an interview is added and the application is still APPLIED or SCREENING or WISHLIST, update to INTERVIEWING
        if (application.getStatus() == ApplicationStatus.APPLIED ||
            application.getStatus() == ApplicationStatus.SCREENING ||
            application.getStatus() == ApplicationStatus.WISHLIST) {
            application.setStatus(ApplicationStatus.INTERVIEWING);
            jobApplicationRepository.save(application);
        }

        Interview saved = interviewRepository.save(interview);
        return InterviewResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<InterviewResponse> getInterviewsByApplication(User user, Long applicationId) {
        // Validate user ownership of application
        jobApplicationService.findApplicationOrThrow(applicationId, user.getId());

        return interviewRepository.findByJobApplicationIdAndJobApplicationUserIdOrderByScheduledAtAsc(applicationId, user.getId())
                .stream()
                .map(InterviewResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public InterviewResponse getInterviewById(User user, Long interviewId) {
        Interview interview = findInterviewOrThrow(interviewId, user.getId());
        return InterviewResponse.fromEntity(interview);
    }

    @Transactional
    public InterviewResponse updateInterview(User user, Long interviewId, UpdateInterviewRequest request) {
        Interview interview = findInterviewOrThrow(interviewId, user.getId());

        interview.setRoundName(request.getRoundName().trim());
        if (request.getInterviewType() != null) {
            interview.setInterviewType(request.getInterviewType());
        }
        interview.setScheduledAt(request.getScheduledAt());
        interview.setInterviewerName(request.getInterviewerName());
        interview.setInterviewerEmail(request.getInterviewerEmail());
        interview.setLocationOrLink(request.getLocationOrLink());
        if (request.getStatus() != null) {
            interview.setStatus(request.getStatus());
        }
        interview.setFeedback(request.getFeedback());
        interview.setNotes(request.getNotes());

        Interview updated = interviewRepository.save(interview);
        return InterviewResponse.fromEntity(updated);
    }

    @Transactional
    public void deleteInterview(User user, Long interviewId) {
        Interview interview = findInterviewOrThrow(interviewId, user.getId());
        interviewRepository.delete(interview);
    }

    public Interview findInterviewOrThrow(Long interviewId, Long userId) {
        return interviewRepository.findByIdAndJobApplicationUserId(interviewId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Interview not found with ID: " + interviewId));
    }
}
