package com.safesphere.backend.risk;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/risk")
public class RiskController {

    private final RiskScoreService riskService;

    public RiskController(RiskScoreService riskService) {
        this.riskService = riskService;
    }

    @PostMapping("/calculate")
    public ResponseEntity<RiskScore> calculate(@RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(riskService.calculateRiskForArea(
                (String) body.get("areaName"),
                ((Number) body.get("latitude")).doubleValue(),
                ((Number) body.get("longitude")).doubleValue(),
                body.containsKey("radiusDeg") ? ((Number) body.get("radiusDeg")).doubleValue() : 0.05));
    }

    @GetMapping
    public ResponseEntity<List<RiskScore>> getAll() {
        return ResponseEntity.ok(riskService.getAllRiskScores());
    }

    @GetMapping("/heatmap")
    public ResponseEntity<List<RiskScore>> getHeatmap(
            @RequestParam double minLat, @RequestParam double maxLat,
            @RequestParam double minLng, @RequestParam double maxLng) {
        return ResponseEntity.ok(riskService.getHeatmapData(minLat, maxLat, minLng, maxLng));
    }

    @GetMapping("/high-risk")
    public ResponseEntity<List<RiskScore>> getHighRisk() {
        return ResponseEntity.ok(riskService.getHighRiskAreas());
    }
}
