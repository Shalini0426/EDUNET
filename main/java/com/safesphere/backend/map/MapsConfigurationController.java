package com.safesphere.backend.map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/maps")
public class MapsConfigurationController {

    private final String apiKey;

    public MapsConfigurationController(
            @Value("${safesphere.google.maps.browser-api-key:${GOOGLE_MAPS_API_KEY:}}") String apiKey) {
        this.apiKey = apiKey;
    }

    @GetMapping("/config")
    public MapsConfiguration configuration() {
        return new MapsConfiguration(apiKey);
    }

    public record MapsConfiguration(String apiKey) { }
}
