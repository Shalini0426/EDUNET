package com.safesphere.backend.auth;

import com.safesphere.backend.account.UserAccountService;
import com.safesphere.backend.security.UserAccountDetailsService;
import com.safesphere.backend.verification.EmailOtpService;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final UserAccountService accounts;
    private final EmailOtpService otpService;
    private final UserAccountDetailsService userDetailsService;
    private final SecurityContextRepository securityContextRepository;
    private final boolean googleEnabled;

    public AuthController(
            UserAccountService accounts,
            EmailOtpService otpService,
            UserAccountDetailsService userDetailsService,
            SecurityContextRepository securityContextRepository,
            @Value("${safesphere.google.client-id:}") String googleClientId,
            @Value("${safesphere.google.client-secret:}") String googleClientSecret) {
        this.accounts = accounts;
        this.otpService = otpService;
        this.userDetailsService = userDetailsService;
        this.securityContextRepository = securityContextRepository;
        this.googleEnabled = !googleClientId.isBlank() && !googleClientSecret.isBlank();
    }

    @GetMapping("/login")
    public String login(Model model) {
        model.addAttribute("googleEnabled", googleEnabled);
        return "login";
    }

    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("registrationForm", new RegistrationForm());
        return "register";
    }

    @PostMapping("/register")
    public String register(
            @Valid @ModelAttribute RegistrationForm registrationForm,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "register";
        }
        try {
            accounts.register(
                    registrationForm.getFullName(),
                    registrationForm.getEmail(),
                    registrationForm.getPassword(),
                    registrationForm.getRole());
        } catch (ResponseStatusException ex) {
            model.addAttribute("registrationError", ex.getReason());
            return "register";
        }

        String email = UserAccountService.normalizeEmail(registrationForm.getEmail());
        try {
            otpService.sendRegistrationCode(email);
            redirectAttributes.addFlashAttribute("notice", "A six-digit verification code was sent to your email.");
        } catch (MailException | IllegalStateException ex) {
            log.warn("Failed to send registration verification email.", ex);
            redirectAttributes.addFlashAttribute(
                    "notice",
                    "Your account was created but email delivery failed. Check SMTP settings, then request another code.");
        }
        redirectAttributes.addAttribute("email", email);
        return "redirect:/verify-email";
    }

    @GetMapping("/verify-email")
    public String verifyEmail(@RequestParam(required = false) String email, Model model) {
        model.addAttribute("email", email == null ? "" : email);
        return "verify-email";
    }

    @PostMapping("/verify-email")
    public String verifyEmail(
            @RequestParam String email,
            @RequestParam String code,
            HttpServletRequest request,
            HttpServletResponse response,
            Model model,
            RedirectAttributes redirectAttributes) {
        try {
            otpService.verify(email, code);
            request.getSession(true);
            request.changeSessionId();
            var userDetails = userDetailsService.loadUserByUsername(UserAccountService.normalizeEmail(email));
            var authentication = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);
            securityContextRepository.saveContext(context, request, response);
            redirectAttributes.addFlashAttribute("notice", "Your email is verified. Welcome to SafeSphere.");
            return "redirect:/dashboard";
        } catch (ResponseStatusException ex) {
            model.addAttribute("email", email);
            model.addAttribute("verificationError", ex.getReason());
            return "verify-email";
        }
    }

    @PostMapping("/resend-otp")
    public String resendOtp(@RequestParam String email, RedirectAttributes redirectAttributes) {
        try {
            otpService.sendRegistrationCode(email);
            redirectAttributes.addFlashAttribute("notice", "A new verification code was sent.");
        } catch (ResponseStatusException ex) {
            redirectAttributes.addFlashAttribute("notice", ex.getReason());
        } catch (MailException | IllegalStateException ex) {
            log.warn("Failed to resend email verification code.", ex);
            redirectAttributes.addFlashAttribute(
                    "notice",
                    "Email delivery failed. Check SMTP settings and try again.");
        }
        redirectAttributes.addAttribute("email", UserAccountService.normalizeEmail(email));
        return "redirect:/verify-email";
    }
}
