package com.safesphere.backend.safezone;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/safe-zones")
public class SafeZoneController {

    private final SafeZoneService safeZoneService;

    public SafeZoneController(SafeZoneService safeZoneService) {
        this.safeZoneService = safeZoneService;
    }

    @PostMapping
    public ResponseEntity<SafeZone> add(@RequestBody SafeZone zone) {
        return ResponseEntity.ok(safeZoneService.addSafeZone(zone));
    }

    @GetMapping
    public ResponseEntity<List<SafeZone>> getAll() {
        return ResponseEntity.ok(safeZoneService.getAllActive());
    }

    @GetMapping("/nearby")
    public ResponseEntity<List<SafeZone>> getNearby(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam(defaultValue = "0.05") double radius) {
        return ResponseEntity.ok(safeZoneService.getNearbySafeZones(lat, lng, radius));
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<SafeZone>> getByType(@PathVariable String type) {
        return ResponseEntity.ok(safeZoneService.getByType(type));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SafeZone> update(@PathVariable Long id, @RequestBody SafeZone zone) {
        return ResponseEntity.ok(safeZoneService.updateSafeZone(id, zone));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        safeZoneService.deleteSafeZone(id);
        return ResponseEntity.noContent().build();
    }
}
