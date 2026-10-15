package com.safesphere.backend.analytics;

import com.safesphere.backend.incident.IncidentReportRepository;
import com.safesphere.backend.incident.ai.AIAnalysisRepository;
import com.safesphere.backend.risk.RiskScoreRepository;
import com.safesphere.backend.safezone.SafeZoneRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AnalyticsService {

    private final IncidentReportRepository incidentRepo;
    private final AIAnalysisRepository aiRepo;
    private final RiskScoreRepository riskRepo;
    private final SafeZoneRepository safeZoneRepo;

    public AnalyticsService(IncidentReportRepository incidentRepo,
                            AIAnalysisRepository aiRepo,
                            RiskScoreRepository riskRepo,
                            SafeZoneRepository safeZoneRepo) {
        this.incidentRepo = incidentRepo;
        this.aiRepo = aiRepo;
        this.riskRepo = riskRepo;
        this.safeZoneRepo = safeZoneRepo;
    }

    public Map<String, Object> getDashboardSummary() {
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalIncidents", incidentRepo.count());
        summary.put("totalSafeZones", safeZoneRepo.count());
        summary.put("totalRiskAreas", riskRepo.count());
        summary.put("incidentsByCategory", toMap(incidentRepo.countByCategory()));
        summary.put("incidentsByStatus", toMap(incidentRepo.countByStatus()));
        summary.put("incidentsBySeverity", toMap(aiRepo.countBySeverity()));
        return summary;
    }

    public List<Object[]> getIncidentsByCategory() {
        return incidentRepo.countByCategory();
    }

    public List<Object[]> getIncidentsByStatus() {
        return incidentRepo.countByStatus();
    }

    public List<Object[]> getIncidentsBySeverity() {
        return aiRepo.countBySeverity();
    }

    private Map<String, Long> toMap(List<Object[]> rows) {
        Map<String, Long> result = new HashMap<>();
        for (Object[] row : rows) {
            result.put(String.valueOf(row[0]), ((Number) row[1]).longValue());
        }
        return result;
    }
}
