package com.safesphere.backend.auth;

import com.safesphere.backend.account.UserAccount;
import com.safesphere.backend.account.UserAccountService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/session")
public class SessionController {

    private final UserAccountService accounts;
    private final CookieCsrfTokenRepository csrfTokenRepository;

    public SessionController(UserAccountService accounts, CookieCsrfTokenRepository csrfTokenRepository) {
        this.accounts = accounts;
        this.csrfTokenRepository = csrfTokenRepository;
    }

    @GetMapping("/csrf")
    public Map<String, String> csrfToken(
            HttpServletRequest request,
            HttpServletResponse response) {
        CsrfToken token = csrfTokenRepository.generateToken(request);
        csrfTokenRepository.saveToken(token, request, response);
        return Map.of("token", token.getToken());
    }

    @GetMapping("/me")
    public SessionUser currentUser(Authentication authentication) {
        return SessionUser.from(accounts.requireByEmail(authentication.getName()));
    }

    @PutMapping("/profile")
    public SessionUser updateProfile(
            Authentication authentication,
            @Valid @RequestBody ProfileUpdate update) {
        return SessionUser.from(accounts.updateProfile(authentication.getName(), update.fullName()));
    }

    public record ProfileUpdate(@NotBlank @Size(max = 100) String fullName) { }

    public record SessionUser(
            Long id,
            String email,
            String fullName,
            String role,
            boolean emailVerified,
            LocalDateTime createdAt) {

        private static SessionUser from(UserAccount account) {
            return new SessionUser(
                    account.getId(),
                    account.getEmail(),
                    account.getFullName(),
                    account.getRole().name(),
                    account.isEmailVerified(),
                    account.getCreatedAt());
        }
    }
}
