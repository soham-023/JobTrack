package com.jobtrack.controller;

import com.jobtrack.dto.response.DashboardStatsResponse;
import com.jobtrack.entity.User;
import com.jobtrack.service.AnalyticsService;
import com.jobtrack.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/analytics")
@Tag(name = "7. Pipeline Analytics", description = "Real-time pipeline metrics, response rates, and application status breakdown")
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final AuthService authService;

    public AnalyticsController(AnalyticsService analyticsService, AuthService authService) {
        this.analyticsService = analyticsService;
        this.authService = authService;
    }

    @GetMapping("/stats")
    @Operation(summary = "Get pipeline analytics & stats", description = "Calculates total/active applications, interview counts, response & offer rates, and status distribution.")
    public ResponseEntity<DashboardStatsResponse> getDashboardStats() {
        User currentUser = authService.getCurrentAuthenticatedUser();
        DashboardStatsResponse stats = analyticsService.getDashboardStats(currentUser);
        return ResponseEntity.ok(stats);
    }
}
