package com.safesphere.backend.admin;

import com.safesphere.backend.incident.IncidentReport;
import com.safesphere.backend.incident.IncidentReportRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminService {

    private final IncidentReviewRepository reviewRepo;
    private final AdminAuditLogRepository auditRepo;
    private final IncidentReportRepository incidentRepo;

    public AdminService(IncidentReviewRepository reviewRepo,
                        AdminAuditLogRepository auditRepo,
                        IncidentReportRepository incidentRepo) {
        this.reviewRepo = reviewRepo;
        this.auditRepo = auditRepo;
        this.incidentRepo = incidentRepo;
    }

    public IncidentReview reviewIncident(Long incidentId, String adminUsername,
                                         IncidentReview.ReviewDecision decision, String remarks) {
        IncidentReport incident = incidentRepo.findById(incidentId)
                .orElseThrow(() -> new RuntimeException("Incident not found: " + incidentId));

        IncidentReview review = reviewRepo.findByIncidentId(incidentId).orElse(new IncidentReview());
        review.setIncident(incident);
        review.setReviewedBy(adminUsername);
        review.setDecision(decision);
        review.setRemarks(remarks);

        switch (decision) {
            case APPROVED -> incident.setStatus(IncidentReport.IncidentStatus.RESOLVED);
            case REJECTED -> incident.setStatus(IncidentReport.IncidentStatus.REJECTED);
            case ESCALATED -> incident.setStatus(IncidentReport.IncidentStatus.UNDER_REVIEW);
            default -> {}
        }
        incidentRepo.save(incident);

        IncidentReview saved = reviewRepo.save(review);
        logAction(adminUsername, "REVIEW_INCIDENT", "IncidentReport", incidentId,
                "Decision: " + decision + " | " + remarks);
        return saved;
    }

    public List<IncidentReport> getPendingIncidents() {
        return incidentRepo.findByStatus(IncidentReport.IncidentStatus.PENDING);
    }

    public List<IncidentReview> getAllReviews() {
        return reviewRepo.findAll();
    }

    public List<AdminAuditLog> getAuditLogs() {
        return auditRepo.findAllByOrderByTimestampDesc();
    }

    public List<AdminAuditLog> getAuditLogsByAdmin(String adminUsername) {
        return auditRepo.findByAdminUsernameOrderByTimestampDesc(adminUsername);
    }

    public void logAction(String adminUsername, String action, String entity, Long targetId, String details) {
        AdminAuditLog log = new AdminAuditLog();
        log.setAdminUsername(adminUsername);
        log.setAction(action);
        log.setTargetEntity(entity);
        log.setTargetId(targetId);
        log.setDetails(details);
        auditRepo.save(log);
    }
}
