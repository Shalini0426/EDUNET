package com.safesphere.backend.admin;

import com.safesphere.backend.incident.IncidentReport;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/pending")
    public ResponseEntity<List<IncidentReport>> getPending() {
        return ResponseEntity.ok(adminService.getPendingIncidents());
    }

    @PostMapping("/review/{incidentId}")
    public ResponseEntity<IncidentReview> review(
            Authentication authentication,
            @PathVariable Long incidentId,
            @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(adminService.reviewIncident(
                incidentId,
                authentication.getName(),
                IncidentReview.ReviewDecision.valueOf(body.get("decision")),
                body.get("remarks")));
    }

    @GetMapping("/reviews")
    public ResponseEntity<List<IncidentReview>> getAllReviews() {
        return ResponseEntity.ok(adminService.getAllReviews());
    }

    @GetMapping("/audit-logs")
    public ResponseEntity<List<AdminAuditLog>> getAuditLogs() {
        return ResponseEntity.ok(adminService.getAuditLogs());
    }

    @GetMapping("/audit-logs/{adminUsername}")
    public ResponseEntity<List<AdminAuditLog>> getAuditLogsByAdmin(@PathVariable String adminUsername) {
        return ResponseEntity.ok(adminService.getAuditLogsByAdmin(adminUsername));
    }
}
