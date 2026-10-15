package com.safesphere.backend.safezone;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SafeZoneService {

    private final SafeZoneRepository safeZoneRepo;

    public SafeZoneService(SafeZoneRepository safeZoneRepo) {
        this.safeZoneRepo = safeZoneRepo;
    }

    public SafeZone addSafeZone(SafeZone zone) {
        return safeZoneRepo.save(zone);
    }

    public List<SafeZone> getAllActive() {
        return safeZoneRepo.findByActiveTrue();
    }

    public List<SafeZone> getNearbySafeZones(double lat, double lng, double radiusDeg) {
        return safeZoneRepo.findNearby(lat - radiusDeg, lat + radiusDeg, lng - radiusDeg, lng + radiusDeg);
    }

    public List<SafeZone> getByType(String type) {
        return safeZoneRepo.findByType(type);
    }

    public SafeZone updateSafeZone(Long id, SafeZone updated) {
        SafeZone zone = safeZoneRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("SafeZone not found: " + id));
        zone.setName(updated.getName());
        zone.setType(updated.getType());
        zone.setLatitude(updated.getLatitude());
        zone.setLongitude(updated.getLongitude());
        zone.setAddress(updated.getAddress());
        zone.setContactNumber(updated.getContactNumber());
        zone.setActive(updated.isActive());
        return safeZoneRepo.save(zone);
    }

    public void deleteSafeZone(Long id) {
        safeZoneRepo.deleteById(id);
    }
}
