package com.safesphere.backend.incident.ai;

import com.safesphere.backend.incident.IncidentReport;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ai_analysis")
public class AIAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "incident_id", nullable = false)
    private IncidentReport incident;

    private String detectedCategory;

    @Enumerated(EnumType.STRING)
    private Severity severity;

    @Column(length = 1000)
    private String summary;

    private Double duplicateProbability;

    private String incidentPattern;

    @Enumerated(EnumType.STRING)
    private Priority priorityRecommendation;

    private LocalDateTime analyzedAt;

    public enum Severity { LOW, MEDIUM, HIGH, CRITICAL }
    public enum Priority { LOW, MEDIUM, HIGH, URGENT }

    @PrePersist
    public void prePersist() {
        this.analyzedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public IncidentReport getIncident() { return incident; }
    public void setIncident(IncidentReport incident) { this.incident = incident; }
    public String getDetectedCategory() { return detectedCategory; }
    public void setDetectedCategory(String detectedCategory) { this.detectedCategory = detectedCategory; }
    public Severity getSeverity() { return severity; }
    public void setSeverity(Severity severity) { this.severity = severity; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    public Double getDuplicateProbability() { return duplicateProbability; }
    public void setDuplicateProbability(Double duplicateProbability) { this.duplicateProbability = duplicateProbability; }
    public String getIncidentPattern() { return incidentPattern; }
    public void setIncidentPattern(String incidentPattern) { this.incidentPattern = incidentPattern; }
    public Priority getPriorityRecommendation() { return priorityRecommendation; }
    public void setPriorityRecommendation(Priority priorityRecommendation) { this.priorityRecommendation = priorityRecommendation; }
    public LocalDateTime getAnalyzedAt() { return analyzedAt; }
}
