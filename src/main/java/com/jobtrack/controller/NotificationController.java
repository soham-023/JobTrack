package com.jobtrack.controller;

import com.jobtrack.dto.response.NotificationResponse;
import com.jobtrack.entity.User;
import com.jobtrack.service.AuthService;
import com.jobtrack.service.InterviewReminderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/notifications")
@Tag(name = "6. Notifications & Reminders", description = "In-app interview alerts, unread counters, and scheduled reminder execution")
public class NotificationController {

    private final InterviewReminderService reminderService;
    private final AuthService authService;

    public NotificationController(InterviewReminderService reminderService, AuthService authService) {
        this.reminderService = reminderService;
        this.authService = authService;
    }

    @GetMapping
    @Operation(summary = "Get user notifications", description = "Retrieves in-app interview alerts and notifications, optionally filtered to unread only.")
    public ResponseEntity<List<NotificationResponse>> getNotifications(
            @RequestParam(defaultValue = "false") boolean unreadOnly
    ) {
        User currentUser = authService.getCurrentAuthenticatedUser();
        List<NotificationResponse> list = reminderService.getUserNotifications(currentUser, unreadOnly);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/unread-count")
    @Operation(summary = "Get unread notification count", description = "Returns badge count of unread notifications for the authenticated user.")
    public ResponseEntity<Map<String, Long>> getUnreadCount() {
        User currentUser = authService.getCurrentAuthenticatedUser();
        long count = reminderService.getUnreadCount(currentUser);
        return ResponseEntity.ok(Collections.singletonMap("unreadCount", count));
    }

    @PatchMapping("/{id}/read")
    @Operation(summary = "Mark notification as read", description = "Marks a specific notification as read.")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        User currentUser = authService.getCurrentAuthenticatedUser();
        reminderService.markAsRead(currentUser, id);
        
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/read-all")
    @Operation(summary = "Mark all notifications as read", description = "Marks all unread notifications for the user as read.")
    public ResponseEntity<Void> markAllAsRead() {
        User currentUser = authService.getCurrentAuthenticatedUser();
        
        reminderService.markAllAsRead(currentUser);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/check-now")
    
    @Operation(summary = "Trigger reminder scan on demand", description = "Executes the background 24-hour lookahead reminder check immediately.")
    public ResponseEntity<Map<String, Object>> triggerManualCheck() {
        
        int count = reminderService.checkAndSendReminders();
        
        return ResponseEntity.ok(Map.of(
                "message", "Reminder check executed successfully",
                "remindersCreated", count
        ));
    }
}
