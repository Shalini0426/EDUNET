package com.safesphere.backend.incident.ai;

import com.safesphere.backend.incident.IncidentReport;
import com.safesphere.backend.incident.IncidentReportRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AIAnalysisService {

    private final AIAnalysisRepository aiRepo;
    private final IncidentReportRepository incidentRepo;

    public AIAnalysisService(AIAnalysisRepository aiRepo, IncidentReportRepository incidentRepo) {
        this.aiRepo = aiRepo;
        this.incidentRepo = incidentRepo;
    }

    public AIAnalysis analyzeIncident(Long incidentId) {
        IncidentReport incident = incidentRepo.findById(incidentId)
                .orElseThrow(() -> new RuntimeException("Incident not found: " + incidentId));

        AIAnalysis analysis = aiRepo.findByIncidentId(incidentId).orElse(new AIAnalysis());
        analysis.setIncident(incident);

        String desc = incident.getDescription() != null ? incident.getDescription().toLowerCase() : "";

        analysis.setDetectedCategory(detectCategory(desc, incident.getCategory()));
        analysis.setSeverity(analyzeSeverity(desc));
        analysis.setSummary(generateSummary(incident));
        analysis.setDuplicateProbability(detectDuplicateProbability(incident));
        analysis.setIncidentPattern(detectPattern(incident));
        analysis.setPriorityRecommendation(recommendPriority(analysis.getSeverity(), analysis.getDuplicateProbability()));

        return aiRepo.save(analysis);
    }

    public Optional<AIAnalysis> getAnalysisByIncident(Long incidentId) {
        return aiRepo.findByIncidentId(incidentId);
    }

    public List<AIAnalysis> getPotentialDuplicates(double threshold) {
        return aiRepo.findPotentialDuplicates(threshold);
    }

    public List<Object[]> getSeverityStats() {
        return aiRepo.countBySeverity();
    }

    private String detectCategory(String desc, String reportedCategory) {
        if (desc.contains("harass") || desc.contains("stalk")) return "Harassment";
        if (desc.contains("assault") || desc.contains("attack") || desc.contains("hit")) return "Physical Assault";
        if (desc.contains("theft") || desc.contains("stolen") || desc.contains("rob")) return "Theft";
        if (desc.contains("unsafe") || desc.contains("dark") || desc.contains("lighting")) return "Unsafe Area";
        if (desc.contains("follow") || desc.contains("chase")) return "Stalking";
        return reportedCategory != null ? reportedCategory : "General";
    }

    private AIAnalysis.Severity analyzeSeverity(String desc) {
        if (desc.contains("weapon") || desc.contains("knife") || desc.contains("gun") || desc.contains("rape")) return AIAnalysis.Severity.CRITICAL;
        if (desc.contains("assault") || desc.contains("attack") || desc.contains("bleed")) return AIAnalysis.Severity.HIGH;
        if (desc.contains("harass") || desc.contains("follow") || desc.contains("threat")) return AIAnalysis.Severity.MEDIUM;
        return AIAnalysis.Severity.LOW;
    }

    private String generateSummary(IncidentReport incident) {
        return String.format("Incident reported at %s on %s. Category: %s. %s",
                incident.getLocation() != null ? incident.getLocation() : "unknown location",
                incident.getIncidentDateTime() != null ? incident.getIncidentDateTime().toLocalDate() : "unknown date",
                incident.getCategory() != null ? incident.getCategory() : "General",
                incident.getDescription() != null && incident.getDescription().length() > 100
                        ? incident.getDescription().substring(0, 100) + "..."
                        : incident.getDescription());
    }

    private double detectDuplicateProbability(IncidentReport incident) {
        List<IncidentReport> similar = incidentRepo.findByCategory(incident.getCategory());
        long nearbyCount = similar.stream()
                .filter(i -> !i.getId().equals(incident.getId()))
                .filter(i -> i.getLatitude() != null && incident.getLatitude() != null
                        && Math.abs(i.getLatitude() - incident.getLatitude()) < 0.01
                        && Math.abs(i.getLongitude() - incident.getLongitude()) < 0.01)
                .count();
        return Math.min(nearbyCount * 0.25, 0.95);
    }

    private String detectPattern(IncidentReport incident) {
        List<IncidentReport> categoryIncidents = incidentRepo.findByCategory(incident.getCategory());
        if (categoryIncidents.size() >= 5) return "Recurring " + incident.getCategory() + " pattern detected in area";
        if (categoryIncidents.size() >= 3) return "Emerging pattern of " + incident.getCategory();
        return "Isolated incident";
    }

    private AIAnalysis.Priority recommendPriority(AIAnalysis.Severity severity, double duplicateProb) {
        if (severity == AIAnalysis.Severity.CRITICAL) return AIAnalysis.Priority.URGENT;
        if (severity == AIAnalysis.Severity.HIGH) return AIAnalysis.Priority.HIGH;
        if (severity == AIAnalysis.Severity.MEDIUM && duplicateProb > 0.5) return AIAnalysis.Priority.HIGH;
        if (severity == AIAnalysis.Severity.MEDIUM) return AIAnalysis.Priority.MEDIUM;
        return AIAnalysis.Priority.LOW;
    }
}
