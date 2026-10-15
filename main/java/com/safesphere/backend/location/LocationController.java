package com.safesphere.backend.location;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/location")
public class LocationController {

    private final LocationTrackingService service;

    public LocationController(LocationTrackingService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserLocation updateLocation(
            Authentication authentication,
            @Valid @RequestBody LocationUpdateRequest request) {
        return service.recordLocation(
                authentication.getName(),
                request.latitude(),
                request.longitude(),
                request.accuracy(),
                request.speed(),
                request.heading(),
                request.journeyId(),
                Boolean.TRUE.equals(request.emergency())
        );
    }

    @GetMapping("/latest")
    public ResponseEntity<UserLocation> getLatest(Authentication authentication) {
        return service.getLatestLocation(authentication.getName())
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @GetMapping("/history")
    public List<UserLocation> getHistory(Authentication authentication) {
        return service.getLocationHistory(authentication.getName());
    }

    @GetMapping("/journey/{journeyId}")
    public List<UserLocation> getJourneyTrail(
            Authentication authentication,
            @PathVariable String journeyId) {
        return service.getJourneyLocations(authentication.getName(), journeyId);
    }

    @GetMapping("/user/{email}")
    public ResponseEntity<UserLocation> getUserLatestLocation(
            Authentication authentication,
            @PathVariable String email) {
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
        if (!isAdmin && !authentication.getName().equalsIgnoreCase(email)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not authorized to view this user's location.");
        }
        return service.getLatestLocation(email)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    public record LocationUpdateRequest(
            @NotNull @Min(-90) @Max(90) Double latitude,
            @NotNull @Min(-180) @Max(180) Double longitude,
            Double accuracy,
            Double speed,
            Double heading,
            String journeyId,
            Boolean emergency
    ) {}
}

