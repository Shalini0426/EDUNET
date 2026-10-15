package com.safesphere.backend.contact;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/emergency-contacts")
public class EmergencyContactController {

    private final EmergencyContactService service;

    public EmergencyContactController(EmergencyContactService service) {
        this.service = service;
    }

    @GetMapping
    public List<EmergencyContact> getAll(Authentication authentication) {
        return service.findAll(authentication.getName());
    }

    @GetMapping("/{id}")
    public EmergencyContact getById(Authentication authentication, @PathVariable Long id) {
        return service.findById(authentication.getName(), id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EmergencyContact create(
            Authentication authentication,
            @Valid @RequestBody ContactRequest request) {
        return service.create(authentication.getName(), request.name(), request.phoneNumber(), request.relationship());
    }

    @PutMapping("/{id}")
    public EmergencyContact update(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody ContactRequest request) {
        return service.update(authentication.getName(), id, request.name(), request.phoneNumber(), request.relationship());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(Authentication authentication, @PathVariable Long id) {
        service.delete(authentication.getName(), id);
    }

    public record ContactRequest(
            @NotBlank @Size(max = 100) String name,
            @NotBlank @Size(max = 40) String phoneNumber,
            @Size(max = 80) String relationship) { }
}
