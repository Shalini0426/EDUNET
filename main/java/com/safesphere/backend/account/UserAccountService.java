package com.safesphere.backend.account;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;

@Service
public class UserAccountService {

    private final UserAccountRepository repository;
    private final PasswordEncoder passwordEncoder;

    public UserAccountService(UserAccountRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserAccount register(String fullName, String email, String rawPassword) {
        return register(fullName, email, rawPassword, AccountRole.USER);
    }

    @Transactional
    public UserAccount register(String fullName, String email, String rawPassword, AccountRole requestedRole) {
        if (rawPassword.getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password must be at most 72 UTF-8 bytes.");
        }
        String normalizedEmail = normalizeEmail(email);
        if (repository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "An account with this email already exists.");
        }
        UserAccount account = new UserAccount();
        account.setFullName(fullName.trim());
        account.setEmail(normalizedEmail);
        account.setPasswordHash(passwordEncoder.encode(rawPassword));
        account.setRole(requestedRole == AccountRole.GUEST ? AccountRole.GUEST : AccountRole.USER);
        account.setEmailVerified(false);
        return repository.save(account);
    }

    @Transactional(readOnly = true)
    public UserAccount requireByEmail(String email) {
        return repository.findByEmailIgnoreCase(normalizeEmail(email))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found."));
    }

    @Transactional(readOnly = true)
    public UserAccount requireById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found."));
    }

    @Transactional(readOnly = true)
    public List<UserAccount> findAll() {
        return repository.findAll();
    }

    @Transactional
    public UserAccount updateProfile(String currentEmail, String fullName) {
        UserAccount account = requireByEmail(currentEmail);
        account.setFullName(fullName.trim());
        return repository.save(account);
    }

    @Transactional
    public UserAccount updateManagedAccount(Long id, String fullName, AccountRole role, boolean emailVerified) {
        UserAccount account = requireById(id);
        account.setFullName(fullName.trim());
        account.setRole(role);
        account.setEmailVerified(emailVerified);
        return repository.save(account);
    }

    @Transactional
    public UserAccount createManagedAccount(String fullName, String email, String password, AccountRole role) {
        UserAccount account = register(fullName, email, password);
        account.setRole(role);
        account.setEmailVerified(true);
        return repository.save(account);
    }

    @Transactional
    public void deleteManagedAccount(Long id, String requesterEmail) {
        UserAccount account = requireById(id);
        if (account.getEmail().equalsIgnoreCase(requesterEmail)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You cannot delete your own administrator account.");
        }
        repository.delete(account);
    }

    @Transactional
    public UserAccount findOrCreateGoogleUser(String email, String fullName, String subject) {
        String normalizedEmail = normalizeEmail(email);
        UserAccount account = repository.findByGoogleSubject(subject)
                .or(() -> repository.findByEmailIgnoreCase(normalizedEmail))
                .orElseGet(() -> {
                    UserAccount newAccount = new UserAccount();
                    newAccount.setEmail(normalizedEmail);
                    newAccount.setFullName(fullName == null || fullName.isBlank() ? normalizedEmail : fullName);
                    newAccount.setPasswordHash(passwordEncoder.encode(java.util.UUID.randomUUID().toString()));
                    newAccount.setRole(AccountRole.USER);
                    newAccount.setEmailVerified(true);
                    return newAccount;
                });
        account.setGoogleSubject(subject);
        account.setEmailVerified(true);
        if (account.getFullName() == null || account.getFullName().isBlank()) {
            account.setFullName(fullName == null || fullName.isBlank() ? normalizedEmail : fullName);
        }
        return repository.save(account);
    }

    @Transactional
    public UserAccount markEmailVerified(String email) {
        UserAccount account = requireByEmail(email);
        account.setEmailVerified(true);
        return repository.save(account);
    }

    @Transactional
    public void bootstrapAdministrator(String email, String rawPassword) {
        if (email == null || email.isBlank() || rawPassword == null || rawPassword.isBlank()) {
            return;
        }
        if (repository.existsByRole(AccountRole.ADMIN)) {
            return;
        }
        String normalizedEmail = normalizeEmail(email);
        UserAccount account = repository.findByEmailIgnoreCase(normalizedEmail).orElseGet(UserAccount::new);
        account.setEmail(normalizedEmail);
        account.setFullName(account.getFullName() == null || account.getFullName().isBlank()
                ? "SafeSphere Administrator" : account.getFullName());
        account.setPasswordHash(passwordEncoder.encode(rawPassword));
        account.setRole(AccountRole.ADMIN);
        account.setEmailVerified(true);
        repository.save(account);
    }

    public static String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
