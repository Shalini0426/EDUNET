package com.safesphere.backend.admin;

import com.safesphere.backend.incident.IncidentReport;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "incident_reviews")
public class IncidentReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "incident_id", nullable = false)
    private IncidentReport incident;

    private String reviewedBy;

    @Enumerated(EnumType.STRING)
    private ReviewDecision decision;

    @Column(length = 1000)
    private String remarks;

    private LocalDateTime reviewedAt;

    public enum ReviewDecision { APPROVED, REJECTED, ESCALATED, PENDING }

    @PrePersist
    public void prePersist() {
        this.reviewedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public IncidentReport getIncident() { return incident; }
    public void setIncident(IncidentReport incident) { this.incident = incident; }
    public String getReviewedBy() { return reviewedBy; }
    public void setReviewedBy(String reviewedBy) { this.reviewedBy = reviewedBy; }
    public ReviewDecision getDecision() { return decision; }
    public void setDecision(ReviewDecision decision) { this.decision = decision; }
    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
    public LocalDateTime getReviewedAt() { return reviewedAt; }
}
