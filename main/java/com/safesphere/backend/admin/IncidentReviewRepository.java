package com.safesphere.backend.admin;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface IncidentReviewRepository extends JpaRepository<IncidentReview, Long> {
    Optional<IncidentReview> findByIncidentId(Long incidentId);
    List<IncidentReview> findByDecision(IncidentReview.ReviewDecision decision);
    List<IncidentReview> findByReviewedBy(String adminUsername);
}
