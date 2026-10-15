package com.safesphere.backend.incident.ai;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ai")
public class AIController {

    private final AIAnalysisService aiService;

    public AIController(AIAnalysisService aiService) {
        this.aiService = aiService;
    }

    @PostMapping("/analyze/{incidentId}")
    public ResponseEntity<AIAnalysis> analyze(@PathVariable Long incidentId) {
        return ResponseEntity.ok(aiService.analyzeIncident(incidentId));
    }

    @GetMapping("/incident/{incidentId}")
    public ResponseEntity<AIAnalysis> getAnalysis(@PathVariable Long incidentId) {
        return aiService.getAnalysisByIncident(incidentId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/duplicates")
    public ResponseEntity<List<AIAnalysis>> getDuplicates(
            @RequestParam(defaultValue = "0.5") double threshold) {
        return ResponseEntity.ok(aiService.getPotentialDuplicates(threshold));
    }

    @GetMapping("/severity-stats")
    public ResponseEntity<List<Object[]>> getSeverityStats() {
        return ResponseEntity.ok(aiService.getSeverityStats());
    }
}
