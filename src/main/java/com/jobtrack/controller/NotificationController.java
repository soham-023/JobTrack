package com.jobtrack.controller;

import com.jobtrack.dto.response.NotificationResponse;
import com.jobtrack.entity.User;
import com.jobtrack.service.AuthService;
import com.jobtrack.service.InterviewReminderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final InterviewReminderService reminderService;
    private final AuthService authService;

    public NotificationController(InterviewReminderService reminderService, AuthService authService) {
        this.reminderService = reminderService;
        this.authService = authService;
    }

    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getNotifications(
            @RequestParam(defaultValue = "false") boolean unreadOnly
    ) {
        User currentUser = authService.getCurrentAuthenticatedUser();
        List<NotificationResponse> list = reminderService.getUserNotifications(currentUser, unreadOnly);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount() {
        User currentUser = authService.getCurrentAuthenticatedUser();
        long count = reminderService.getUnreadCount(currentUser);
        return ResponseEntity.ok(Collections.singletonMap("unreadCount", count));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        User currentUser = authService.getCurrentAuthenticatedUser();
        reminderService.markAsRead(currentUser, id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead() {
        User currentUser = authService.getCurrentAuthenticatedUser();
        reminderService.markAllAsRead(currentUser);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/check-now")
    public ResponseEntity<Map<String, Object>> triggerManualCheck() {
        int count = reminderService.checkAndSendReminders();
        return ResponseEntity.ok(Map.of(
                "message", "Reminder check executed successfully",
                "remindersCreated", count
        ));
    }
}
