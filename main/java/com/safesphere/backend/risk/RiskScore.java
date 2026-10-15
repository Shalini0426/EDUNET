package com.safesphere.backend.risk;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "risk_scores")
public class RiskScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String areaName;
    private Double latitude;
    private Double longitude;
    private Double score;

    @Enumerated(EnumType.STRING)
    private RiskLevel riskLevel;

    private int incidentCount;
    private LocalDateTime calculatedAt;

    public enum RiskLevel { SAFE, LOW, MODERATE, HIGH, CRITICAL }

    @PrePersist
    @PreUpdate
    public void preUpdate() {
        this.calculatedAt = LocalDateTime.now();
        this.riskLevel = computeLevel(this.score);
    }

    private RiskLevel computeLevel(Double score) {
        if (score == null) return RiskLevel.SAFE;
        if (score >= 8) return RiskLevel.CRITICAL;
        if (score >= 6) return RiskLevel.HIGH;
        if (score >= 4) return RiskLevel.MODERATE;
        if (score >= 2) return RiskLevel.LOW;
        return RiskLevel.SAFE;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getAreaName() { return areaName; }
    public void setAreaName(String areaName) { this.areaName = areaName; }
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    public Double getScore() { return score; }
    public void setScore(Double score) { this.score = score; }
    public RiskLevel getRiskLevel() { return riskLevel; }
    public int getIncidentCount() { return incidentCount; }
    public void setIncidentCount(int incidentCount) { this.incidentCount = incidentCount; }
    public LocalDateTime getCalculatedAt() { return calculatedAt; }
}
