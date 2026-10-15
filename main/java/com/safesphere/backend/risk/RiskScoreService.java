package com.safesphere.backend.risk;

import com.safesphere.backend.incident.IncidentReport;
import com.safesphere.backend.incident.IncidentReportRepository;
import com.safesphere.backend.incident.ai.AIAnalysis;
import com.safesphere.backend.incident.ai.AIAnalysisRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RiskScoreService {

    private final RiskScoreRepository riskRepo;
    private final IncidentReportRepository incidentRepo;
    private final AIAnalysisRepository aiRepo;

    public RiskScoreService(RiskScoreRepository riskRepo,
                            IncidentReportRepository incidentRepo,
                            AIAnalysisRepository aiRepo) {
        this.riskRepo = riskRepo;
        this.incidentRepo = incidentRepo;
        this.aiRepo = aiRepo;
    }

    public RiskScore calculateRiskForArea(String areaName, double lat, double lng, double radiusDeg) {
        List<IncidentReport> incidents = incidentRepo.findByLocationBounds(
                lat - radiusDeg, lat + radiusDeg, lng - radiusDeg, lng + radiusDeg);

        double score = 0;
        for (IncidentReport incident : incidents) {
            AIAnalysis analysis = aiRepo.findByIncidentId(incident.getId()).orElse(null);
            if (analysis != null) {
                score += switch (analysis.getSeverity()) {
                    case CRITICAL -> 3.0;
                    case HIGH -> 2.0;
                    case MEDIUM -> 1.0;
                    case LOW -> 0.5;
                };
            } else {
                score += 0.5;
            }
        }
        score = Math.min(score, 10.0);

        RiskScore riskScore = riskRepo.findByAreaName(areaName).orElse(new RiskScore());
        riskScore.setAreaName(areaName);
        riskScore.setLatitude(lat);
        riskScore.setLongitude(lng);
        riskScore.setScore(score);
        riskScore.setIncidentCount(incidents.size());
        return riskRepo.save(riskScore);
    }

    public List<RiskScore> getAllRiskScores() {
        return riskRepo.findAllOrderByScoreDesc();
    }

    public List<RiskScore> getHeatmapData(double minLat, double maxLat, double minLng, double maxLng) {
        return riskRepo.findByBounds(minLat, maxLat, minLng, maxLng);
    }

    public List<RiskScore> getHighRiskAreas() {
        return riskRepo.findByRiskLevel(RiskScore.RiskLevel.HIGH);
    }
}
