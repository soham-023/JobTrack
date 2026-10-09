package com.jobtrack.service;

import com.jobtrack.dto.response.NotificationResponse;
import com.jobtrack.entity.Interview;
import com.jobtrack.entity.JobApplication;
import com.jobtrack.entity.Notification;
import com.jobtrack.entity.User;
import com.jobtrack.entity.enums.InterviewStatus;
import com.jobtrack.exception.ResourceNotFoundException;
import com.jobtrack.repository.InterviewRepository;
import com.jobtrack.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class InterviewReminderService {

    private static final Logger log = LoggerFactory.getLogger(InterviewReminderService.class);

    private final InterviewRepository interviewRepository;
    private final NotificationRepository notificationRepository;

    @Value("${application.reminders.lookahead-hours:24}")
    private int lookaheadHours;

    public InterviewReminderService(InterviewRepository interviewRepository,
                                  NotificationRepository notificationRepository) {
        this.interviewRepository = interviewRepository;
        this.notificationRepository = notificationRepository;
    }

    /**
     * Runs on a configurable cron schedule (default: top of every hour).
     * Scans for any interviews happening in the next 24 hours that haven't had a reminder sent.
     */
    @Scheduled(cron = "${application.reminders.cron:0 0 * * * *}")
    @Transactional
    public int checkAndSendReminders() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime windowEnd = now.plusHours(lookaheadHours);

        List<Interview> upcomingInterviews = interviewRepository
                .findByStatusAndReminderSentFalseAndScheduledAtBetween(InterviewStatus.SCHEDULED, now, windowEnd);

        if (upcomingInterviews.isEmpty()) {
            log.debug("Interview reminder check completed: 0 reminders needed.");
            return 0;
        }

        log.info("Found {} upcoming interview(s) within {} hours requiring reminders.", upcomingInterviews.size(), lookaheadHours);
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy h:mm a");

        int generatedCount = 0;
        for (Interview interview : upcomingInterviews) {
            JobApplication app = interview.getJobApplication();
            if (app == null || app.getUser() == null) {
                continue;
            }

            User user = app.getUser();
            Duration duration = Duration.between(now, interview.getScheduledAt());
            long hoursRemaining = duration.toHours();
            long minutesRemaining = duration.toMinutes() % 60;

            String timeDesc = hoursRemaining > 0
                    ? hoursRemaining + " hour(s) and " + minutesRemaining + " min"
                    : minutesRemaining + " min";

            String linkInfo = interview.getLocationOrLink() != null && !interview.getLocationOrLink().isBlank()
                    ? " Link/Location: " + interview.getLocationOrLink()
                    : "";

            String title = "🔔 Upcoming Interview: " + interview.getRoundName() + " with " + app.getCompanyName();
            String message = String.format(
                    "Your %s for the %s position at %s is scheduled in %s (on %s).%s",
                    interview.getRoundName(),
                    app.getJobTitle(),
                    app.getCompanyName(),
                    timeDesc,
                    interview.getScheduledAt().format(timeFormatter),
                    linkInfo
            );

            Notification notification = new Notification(user, interview, title, message);
            notificationRepository.save(notification);

            interview.setReminderSent(true);
            interviewRepository.save(interview);
            generatedCount++;

            log.info("🔔 [INTERVIEW ALERT] Created reminder for user '{}': {} in {}",
                    user.getEmail(), title, timeDesc);
        }

        return generatedCount;
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> getUserNotifications(User user, boolean unreadOnly) {
        List<Notification> list = unreadOnly
                ? notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(user.getId())
                : notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getId());

        return list.stream()
                .map(NotificationResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(User user) {
        return notificationRepository.countByUserIdAndIsReadFalse(user.getId());
    }

    @Transactional
    public void markAsRead(User user, Long notificationId) {
        Notification notification = notificationRepository.findByIdAndUserId(notificationId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with ID: " + notificationId));
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    @Transactional
    public void markAllAsRead(User user) {
        List<Notification> unread = notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(user.getId());
        for (Notification n : unread) {
            n.setRead(true);
        }
        notificationRepository.saveAll(unread);
    }
}
