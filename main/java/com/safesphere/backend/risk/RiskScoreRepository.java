package com.safesphere.backend.risk;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface RiskScoreRepository extends JpaRepository<RiskScore, Long> {

    Optional<RiskScore> findByAreaName(String areaName);

    List<RiskScore> findByRiskLevel(RiskScore.RiskLevel riskLevel);

    @Query("SELECT r FROM RiskScore r WHERE r.latitude BETWEEN :minLat AND :maxLat AND r.longitude BETWEEN :minLng AND :maxLng")
    List<RiskScore> findByBounds(double minLat, double maxLat, double minLng, double maxLng);

    @Query("SELECT r FROM RiskScore r ORDER BY r.score DESC")
    List<RiskScore> findAllOrderByScoreDesc();
}
