package com.safesphere.backend.auth;

import jakarta.validation.constraints.Email;
import com.safesphere.backend.account.AccountRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class RegistrationForm {

    @NotBlank
    @Size(max = 100)
    private String fullName;

    @NotBlank
    @Email
    @Size(max = 254)
    private String email;

    @NotBlank
    @Size(min = 12, max = 72)
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z\\d]).{12,72}$",
            message = "Use at least 12 characters with upper- and lowercase letters, a number, and a symbol.")
    private String password;
    private AccountRole role = AccountRole.USER;

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public AccountRole getRole() { return role; }
    public void setRole(AccountRole role) { this.role = role == AccountRole.GUEST ? AccountRole.GUEST : AccountRole.USER; }
}
