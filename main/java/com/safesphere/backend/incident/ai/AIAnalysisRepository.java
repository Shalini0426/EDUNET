package com.safesphere.backend.incident.ai;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface AIAnalysisRepository extends JpaRepository<AIAnalysis, Long> {

    Optional<AIAnalysis> findByIncidentId(Long incidentId);

    List<AIAnalysis> findBySeverity(AIAnalysis.Severity severity);

    @Query("SELECT a FROM AIAnalysis a WHERE a.duplicateProbability >= :threshold")
    List<AIAnalysis> findPotentialDuplicates(double threshold);

    @Query("SELECT a.severity, COUNT(a) FROM AIAnalysis a GROUP BY a.severity")
    List<Object[]> countBySeverity();
}
