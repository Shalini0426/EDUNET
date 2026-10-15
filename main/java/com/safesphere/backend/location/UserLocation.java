package com.safesphere.backend.location;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.safesphere.backend.account.UserAccount;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_locations", indexes = {
        @Index(name = "idx_user_locations_user", columnList = "user_id"),
        @Index(name = "idx_user_locations_time", columnList = "timestamp")
})
public class UserLocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserAccount user;

    @Column(nullable = false)
    private double latitude;

    @Column(nullable = false)
    private double longitude;

    private Double accuracy;
    private Double speed;
    private Double heading;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(length = 64)
    private String journeyId;

    @Column(nullable = false)
    private boolean emergency;

    @PrePersist
    void prePersist() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
    }

    public Long getId() { return id; }
    @JsonIgnore
    public UserAccount getUser() { return user; }
    public void setUser(UserAccount user) { this.user = user; }
    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
    public Double getAccuracy() { return accuracy; }
    public void setAccuracy(Double accuracy) { this.accuracy = accuracy; }
    public Double getSpeed() { return speed; }
    public void setSpeed(Double speed) { this.speed = speed; }
    public Double getHeading() { return heading; }
    public void setHeading(Double heading) { this.heading = heading; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    public String getJourneyId() { return journeyId; }
    public void setJourneyId(String journeyId) { this.journeyId = journeyId; }
    public boolean isEmergency() { return emergency; }
    public void setEmergency(boolean emergency) { this.emergency = emergency; }
}

