package com.safesphere.backend.safezone;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SafeZoneRepository extends JpaRepository<SafeZone, Long> {

    List<SafeZone> findByActiveTrue();

    List<SafeZone> findByType(String type);

    @Query("SELECT s FROM SafeZone s WHERE s.active = true AND s.latitude BETWEEN :minLat AND :maxLat AND s.longitude BETWEEN :minLng AND :maxLng")
    List<SafeZone> findNearby(double minLat, double maxLat, double minLng, double maxLng);
}
