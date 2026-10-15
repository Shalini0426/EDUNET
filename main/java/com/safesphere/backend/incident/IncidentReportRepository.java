package com.safesphere.backend.incident;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface IncidentReportRepository extends JpaRepository<IncidentReport, Long> {

    @Override
    @EntityGraph(attributePaths = "owner")
    java.util.Optional<IncidentReport> findById(Long id);

    List<IncidentReport> findByStatus(IncidentReport.IncidentStatus status);

    List<IncidentReport> findAllByOwner_EmailIgnoreCaseOrderByCreatedAtDesc(String email);

    java.util.Optional<IncidentReport> findByIdAndOwner_EmailIgnoreCase(Long id, String email);

    List<IncidentReport> findByCategory(String category);

    List<IncidentReport> findByAnonymousFalseAndReportedBy(String reportedBy);

    @Query("SELECT i FROM IncidentReport i WHERE i.latitude BETWEEN :minLat AND :maxLat AND i.longitude BETWEEN :minLng AND :maxLng")
    List<IncidentReport> findByLocationBounds(double minLat, double maxLat, double minLng, double maxLng);

    @Query("SELECT i.category, COUNT(i) FROM IncidentReport i GROUP BY i.category")
    List<Object[]> countByCategory();

    @Query("SELECT i.status, COUNT(i) FROM IncidentReport i GROUP BY i.status")
    List<Object[]> countByStatus();
}
