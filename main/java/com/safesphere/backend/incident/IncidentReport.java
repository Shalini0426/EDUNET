package com.safesphere.backend.incident;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.safesphere.backend.account.UserAccount;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "incident_reports")
public class IncidentReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String category;
    private String location;
    private Double latitude;
    private Double longitude;
    private LocalDateTime incidentDateTime;

    @Column(length = 2000)
    private String description;

    private boolean anonymous;
    private String reportedBy;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private UserAccount owner;

    @OneToMany(mappedBy = "incident", cascade = CascadeType.REMOVE)
    private List<IncidentEvidence> evidence = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private IncidentStatus status;

    private LocalDateTime createdAt;

    public enum IncidentStatus {
        PENDING, UNDER_REVIEW, RESOLVED, REJECTED
    }

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) this.status = IncidentStatus.PENDING;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    public LocalDateTime getIncidentDateTime() { return incidentDateTime; }
    public void setIncidentDateTime(LocalDateTime incidentDateTime) { this.incidentDateTime = incidentDateTime; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public boolean isAnonymous() { return anonymous; }
    public void setAnonymous(boolean anonymous) { this.anonymous = anonymous; }
    public String getReportedBy() { return reportedBy; }
    public void setReportedBy(String reportedBy) { this.reportedBy = reportedBy; }
    @JsonIgnore
    public UserAccount getOwner() { return owner; }
    public void setOwner(UserAccount owner) { this.owner = owner; }
    public IncidentStatus getStatus() { return status; }
    public void setStatus(IncidentStatus status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
