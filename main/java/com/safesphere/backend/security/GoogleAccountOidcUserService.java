package com.safesphere.backend.security;

import com.safesphere.backend.account.UserAccountService;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class GoogleAccountOidcUserService implements OAuth2UserService<OidcUserRequest, OidcUser> {

    private final OidcUserService delegate = new OidcUserService();
    private final UserAccountService accounts;

    public GoogleAccountOidcUserService(UserAccountService accounts) {
        this.accounts = accounts;
    }

    @Override
    public OidcUser loadUser(OidcUserRequest request) throws OAuth2AuthenticationException {
        return persistGoogleAccount(delegate.loadUser(request));
    }

    OidcUser persistGoogleAccount(OidcUser googleUser) throws OAuth2AuthenticationException {
        String email = googleUser.getEmail();
        String subject = googleUser.getSubject();
        if (email == null || !Boolean.TRUE.equals(googleUser.getClaimAsBoolean("email_verified"))
                || subject == null) {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("invalid_user_info"),
                    "Google did not provide a verified email address.");
        }

        var account = accounts.findOrCreateGoogleUser(
                email, googleUser.getClaimAsString("name"), subject);
        var authorities = new ArrayList<GrantedAuthority>(googleUser.getAuthorities());
        authorities.add(new SimpleGrantedAuthority("ROLE_" + account.getRole().name()));
        return new DefaultOidcUser(
                authorities,
                googleUser.getIdToken(),
                googleUser.getUserInfo(),
                "email");
    }
}
