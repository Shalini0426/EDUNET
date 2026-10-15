package com.safesphere.backend.location;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserLocationRepository extends JpaRepository<UserLocation, Long> {

    Optional<UserLocation> findFirstByUserEmailIgnoreCaseOrderByTimestampDesc(String email);

    List<UserLocation> findTop50ByUserEmailIgnoreCaseOrderByTimestampDesc(String email);

    List<UserLocation> findAllByUserEmailIgnoreCaseAndJourneyIdOrderByTimestampAsc(String email, String journeyId);
}

