package com.safesphere.backend.incident;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IncidendEvidenceRepository extends JpaRepository<IncidentEvidence, Long> {
    List<IncidentEvidence> findByIncidentId(Long incidentId);
}
