package com.safesphere.backend.incident;

import com.safesphere.backend.incident.ai.AIAnalysisService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.nio.file.*;
import java.util.List;
import java.util.UUID;

@Service
public class IncidentService {

    private final IncidentReportRepository incidentRepo;
    private final IncidendEvidenceRepository evidenceRepo;
    private final AIAnalysisService aiService;

    private static final String UPLOAD_DIR = "uploads/evidence/";

    public IncidentService(IncidentReportRepository incidentRepo,
                           IncidendEvidenceRepository evidenceRepo,
                           AIAnalysisService aiService) {
        this.incidentRepo = incidentRepo;
        this.evidenceRepo = evidenceRepo;
        this.aiService = aiService;
    }

    public IncidentReport submitIncident(IncidentReport report) {
        if (report.isAnonymous()) report.setReportedBy(null);
        IncidentReport saved = incidentRepo.save(report);
        aiService.analyzeIncident(saved.getId());
        return saved;
    }

    public IncidentEvidence uploadEvidence(Long incidentId, MultipartFile file) throws IOException {
        IncidentReport incident = incidentRepo.findById(incidentId)
                .orElseThrow(() -> new RuntimeException("Incident not found: " + incidentId));

        Files.createDirectories(Paths.get(UPLOAD_DIR));
        String originalName = Paths.get(file.getOriginalFilename() == null ? "evidence" : file.getOriginalFilename())
                .getFileName().toString();
        String fileName = UUID.randomUUID() + "_" + originalName;
        Path filePath = Paths.get(UPLOAD_DIR).resolve(fileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        IncidentEvidence evidence = new IncidentEvidence();
        evidence.setIncident(incident);
        evidence.setFileName(originalName);
        evidence.setFileType(file.getContentType());
        evidence.setFileUrl(filePath.toString());
        return evidenceRepo.save(evidence);
    }

    public List<IncidentReport> getAllIncidents() {
        return incidentRepo.findAll();
    }

    public List<IncidentReport> getIncidentsForOwner(String ownerEmail) {
        return incidentRepo.findAllByOwner_EmailIgnoreCaseOrderByCreatedAtDesc(ownerEmail);
    }

    public IncidentReport getById(Long id) {
        return incidentRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Incident not found."));
    }

    public IncidentReport getOwnedById(String ownerEmail, Long id) {
        return incidentRepo.findByIdAndOwner_EmailIgnoreCase(id, ownerEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Incident not found."));
    }

    public IncidentReport updateOwnedIncident(String ownerEmail, Long id, IncidentReport update) {
        IncidentReport existing = getOwnedById(ownerEmail, id);
        existing.setCategory(update.getCategory());
        existing.setLocation(update.getLocation());
        existing.setLatitude(update.getLatitude());
        existing.setLongitude(update.getLongitude());
        existing.setIncidentDateTime(update.getIncidentDateTime());
        existing.setDescription(update.getDescription());
        existing.setAnonymous(update.isAnonymous());
        existing.setReportedBy(update.isAnonymous() ? null : ownerEmail);
        return incidentRepo.save(existing);
    }

    public void deleteOwnedIncident(String ownerEmail, Long id) {
        IncidentReport report = getOwnedById(ownerEmail, id);
        evidenceRepo.deleteAll(evidenceRepo.findByIncidentId(id));
        incidentRepo.delete(report);
    }

    public void deleteIncident(Long id) {
        IncidentReport report = getById(id);
        evidenceRepo.deleteAll(evidenceRepo.findByIncidentId(id));
        incidentRepo.delete(report);
    }

    public List<IncidentReport> getByStatus(IncidentReport.IncidentStatus status) {
        return incidentRepo.findByStatus(status);
    }

    public List<IncidentEvidence> getEvidenceByIncident(Long incidentId) {
        return evidenceRepo.findByIncidentId(incidentId);
    }

    public IncidentReport updateStatus(Long id, IncidentReport.IncidentStatus status) {
        IncidentReport report = getById(id);
        report.setStatus(status);
        return incidentRepo.save(report);
    }

    public IncidentReport saveIncident(IncidentReport report) {
        return incidentRepo.save(report);
    }
}
