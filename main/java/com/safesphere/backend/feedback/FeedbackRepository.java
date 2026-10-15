package com.safesphere.backend.feedback;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    List<Feedback> findAllByAuthorEmailIgnoreCaseOrderByCreatedAtDesc(String email);
    Optional<Feedback> findByIdAndAuthorEmailIgnoreCase(Long id, String email);

    @Query("select avg(f.rating) from Feedback f")
    Optional<Double> calculateAverageRating();
}
