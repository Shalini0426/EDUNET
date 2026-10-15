package com.safesphere.backend.feedback;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/feedback")
public class FeedbackController {

    private final FeedbackService service;

    public FeedbackController(FeedbackService service) {
        this.service = service;
    }

    @GetMapping("/rating")
    public Map<String, Double> rating() {
        return Map.of("averageRating", service.averageRating());
    }

    @GetMapping
    public List<Feedback> getMine(Authentication authentication) {
        if (isAdmin(authentication)) {
            return service.findAll();
        }
        return service.findAllForUser(authentication.getName());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Feedback create(Authentication authentication, @Valid @RequestBody FeedbackRequest request) {
        return service.create(authentication.getName(), request.rating(), request.comments());
    }

    @PutMapping("/{id}")
    public Feedback update(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody FeedbackRequest request) {
        return service.update(authentication.getName(), id, request.rating(), request.comments(), isAdmin(authentication));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(Authentication authentication, @PathVariable Long id) {
        service.delete(authentication.getName(), id, isAdmin(authentication));
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
    }

    public record FeedbackRequest(
            @Min(1) @Max(5) int rating,
            @NotBlank @Size(max = 2000) String comments) { }
}
