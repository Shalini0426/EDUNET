package com.safesphere.backend.security;

import com.safesphere.backend.account.UserAccountService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GoogleAccountOAuth2UserService extends DefaultOAuth2UserService {

    private final UserAccountService accounts;

    public GoogleAccountOAuth2UserService(UserAccountService accounts) {
        this.accounts = accounts;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest request) throws OAuth2AuthenticationException {
        OAuth2User googleUser = super.loadUser(request);
        String email = googleUser.getAttribute("email");
        Boolean verified = googleUser.getAttribute("email_verified");
        String subject = googleUser.getAttribute("sub");
        if (email == null || !Boolean.TRUE.equals(verified) || subject == null) {
            throw new BadCredentialsException("Google did not provide a verified email address.");
        }

        var account = accounts.findOrCreateGoogleUser(email, googleUser.getAttribute("name"), subject);
        return new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority("ROLE_" + account.getRole().name())),
                googleUser.getAttributes(),
                "email");
    }
}
