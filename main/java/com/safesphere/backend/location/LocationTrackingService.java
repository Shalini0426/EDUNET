package com.safesphere.backend.location;

import com.safesphere.backend.account.UserAccount;
import com.safesphere.backend.account.UserAccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class LocationTrackingService {

    private static final Logger log = LoggerFactory.getLogger(LocationTrackingService.class);

    private final UserLocationRepository repository;
    private final UserAccountService accounts;
    private final ObjectProvider<SimpMessagingTemplate> messagingTemplateProvider;

    public LocationTrackingService(
            UserLocationRepository repository,
            UserAccountService accounts,
            ObjectProvider<SimpMessagingTemplate> messagingTemplateProvider) {
        this.repository = repository;
        this.accounts = accounts;
        this.messagingTemplateProvider = messagingTemplateProvider;
    }

    @Transactional
    public UserLocation recordLocation(
            String email,
            double latitude,
            double longitude,
            Double accuracy,
            Double speed,
            Double heading,
            String journeyId,
            boolean emergency) {

        UserAccount user = accounts.requireByEmail(email);

        UserLocation location = new UserLocation();
        location.setUser(user);
        location.setLatitude(latitude);
        location.setLongitude(longitude);
        location.setAccuracy(accuracy);
        location.setSpeed(speed);
        location.setHeading(heading);
        location.setTimestamp(LocalDateTime.now());
        location.setJourneyId(journeyId);
        location.setEmergency(emergency);

        UserLocation saved = repository.save(location);

        // Broadcast to WebSocket subscribers for live tracking
        SimpMessagingTemplate template = messagingTemplateProvider.getIfAvailable();
        if (template != null) {
            Map<String, Object> payload = Map.of(
                    "userId", user.getId(),
                    "userEmail", user.getEmail(),
                    "userName", user.getFullName(),
                    "latitude", saved.getLatitude(),
                    "longitude", saved.getLongitude(),
                    "accuracy", saved.getAccuracy() == null ? 0.0 : saved.getAccuracy(),
                    "speed", saved.getSpeed() == null ? 0.0 : saved.getSpeed(),
                    "heading", saved.getHeading() == null ? 0.0 : saved.getHeading(),
                    "timestamp", saved.getTimestamp().toString(),
                    "emergency", saved.isEmergency(),
                    "journeyId", saved.getJourneyId() == null ? "" : saved.getJourneyId()
            );

            try {
                template.convertAndSend("/topic/location/" + user.getId(), payload);
                if (emergency) {
                    template.convertAndSend("/topic/emergency/locations", payload);
                }
            } catch (Exception ex) {
                log.warn("Failed to broadcast WebSocket location update: {}", ex.getMessage());
            }
        }

        return saved;
    }

    @Transactional(readOnly = true)
    public Optional<UserLocation> getLatestLocation(String email) {
        return repository.findFirstByUserEmailIgnoreCaseOrderByTimestampDesc(email);
    }

    @Transactional(readOnly = true)
    public List<UserLocation> getLocationHistory(String email) {
        return repository.findTop50ByUserEmailIgnoreCaseOrderByTimestampDesc(email);
    }

    @Transactional(readOnly = true)
    public List<UserLocation> getJourneyLocations(String email, String journeyId) {
        return repository.findAllByUserEmailIgnoreCaseAndJourneyIdOrderByTimestampAsc(email, journeyId);
    }
}

