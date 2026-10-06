package com.jobtrack.controller;

import com.jobtrack.dto.response.DashboardStatsResponse;
import com.jobtrack.entity.User;
import com.jobtrack.service.AnalyticsService;
import com.jobtrack.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final AuthService authService;

    public AnalyticsController(AnalyticsService analyticsService, AuthService authService) {
        this.analyticsService = analyticsService;
        this.authService = authService;
    }

    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsResponse> getDashboardStats() {
        User currentUser = authService.getCurrentAuthenticatedUser();
        DashboardStatsResponse stats = analyticsService.getDashboardStats(currentUser);
        return ResponseEntity.ok(stats);
    }
}
