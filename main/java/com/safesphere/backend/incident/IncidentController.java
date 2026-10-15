package com.safesphere.backend.incident;

import com.safesphere.backend.account.UserAccountService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/incidents")
public class IncidentController {

    private final IncidentService incidentService;
    private final UserAccountService accounts;

    public IncidentController(IncidentService incidentService, UserAccountService accounts) {
        this.incidentService = incidentService;
        this.accounts = accounts;
    }

    @PostMapping
    public ResponseEntity<IncidentReport> submit(
            Authentication authentication,
            @Valid @RequestBody IncidentRequest request) {
        IncidentReport report = request.toEntity();
        report.setOwner(accounts.requireByEmail(authentication.getName()));
        report.setReportedBy(report.isAnonymous() ? null : authentication.getName());
        return ResponseEntity.ok(incidentService.submitIncident(report));
    }

    @PostMapping("/{id}/evidence")
    public ResponseEntity<IncidentEvidence> uploadEvidence(
            Authentication authentication,
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) throws IOException {
        assertCanRead(incidentService.getById(id), authentication);
        return ResponseEntity.ok(incidentService.uploadEvidence(id, file));
    }

    @GetMapping
    public ResponseEntity<List<IncidentReport>> getAll(Authentication authentication) {
        if (isAdmin(authentication)) {
            return ResponseEntity.ok(incidentService.getAllIncidents());
        }
        return ResponseEntity.ok(incidentService.getIncidentsForOwner(authentication.getName()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<IncidentReport> getById(Authentication authentication, @PathVariable Long id) {
        IncidentReport incident = incidentService.getById(id);
        assertCanRead(incident, authentication);
        return ResponseEntity.ok(incident);
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<IncidentReport>> getByStatus(@PathVariable IncidentReport.IncidentStatus status) {
        return ResponseEntity.ok(incidentService.getByStatus(status));
    }

    @GetMapping("/{id}/evidence")
    public ResponseEntity<List<IncidentEvidence>> getEvidence(Authentication authentication, @PathVariable Long id) {
        assertCanRead(incidentService.getById(id), authentication);
        return ResponseEntity.ok(incidentService.getEvidenceByIncident(id));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<IncidentReport> updateStatus(
            @PathVariable Long id,
            @RequestParam IncidentReport.IncidentStatus status) {
        return ResponseEntity.ok(incidentService.updateStatus(id, status));
    }

    @PutMapping("/{id}")
    public ResponseEntity<IncidentReport> update(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody IncidentRequest update) {
        if (isAdmin(authentication)) {
            return ResponseEntity.ok(updateAdminIncident(id, update));
        }
        return ResponseEntity.ok(incidentService.updateOwnedIncident(authentication.getName(), id, update.toEntity()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(Authentication authentication, @PathVariable Long id) {
        if (isAdmin(authentication)) {
            incidentService.deleteIncident(id);
        } else {
            incidentService.deleteOwnedIncident(authentication.getName(), id);
        }
        return ResponseEntity.noContent().build();
    }

    private IncidentReport updateAdminIncident(Long id, IncidentRequest update) {
        IncidentReport existing = incidentService.getById(id);
        IncidentReport changes = update.toEntity();
        existing.setCategory(changes.getCategory());
        existing.setLocation(changes.getLocation());
        existing.setLatitude(changes.getLatitude());
        existing.setLongitude(changes.getLongitude());
        existing.setIncidentDateTime(changes.getIncidentDateTime());
        existing.setDescription(changes.getDescription());
        existing.setAnonymous(changes.isAnonymous());
        return incidentService.saveIncident(existing);
    }

    private void assertCanRead(IncidentReport incident, Authentication authentication) {
        if (!isAdmin(authentication)
                && (incident.getOwner() == null || !incident.getOwner().getEmail().equalsIgnoreCase(authentication.getName()))) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.NOT_FOUND, "Incident not found.");
        }
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
    }

    public record IncidentRequest(
            @NotBlank @Size(max = 100) String category,
            @Size(max = 300) String location,
            Double latitude,
            Double longitude,
            java.time.LocalDateTime incidentDateTime,
            @NotBlank @Size(max = 2000) String description,
            boolean anonymous) {

        private IncidentReport toEntity() {
            IncidentReport report = new IncidentReport();
            report.setCategory(category);
            report.setLocation(location);
            report.setLatitude(latitude);
            report.setLongitude(longitude);
            report.setIncidentDateTime(incidentDateTime);
            report.setDescription(description);
            report.setAnonymous(anonymous);
            return report;
        }
    }
}
