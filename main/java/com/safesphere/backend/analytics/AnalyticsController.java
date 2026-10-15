package com.safesphere.backend.analytics;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboard() {
        return ResponseEntity.ok(analyticsService.getDashboardSummary());
    }

    @GetMapping("/incidents-by-category")
    public ResponseEntity<List<Object[]>> byCategory() {
        return ResponseEntity.ok(analyticsService.getIncidentsByCategory());
    }

    @GetMapping("/incidents-by-status")
    public ResponseEntity<List<Object[]>> byStatus() {
        return ResponseEntity.ok(analyticsService.getIncidentsByStatus());
    }

    @GetMapping("/incidents-by-severity")
    public ResponseEntity<List<Object[]>> bySeverity() {
        return ResponseEntity.ok(analyticsService.getIncidentsBySeverity());
    }
}
