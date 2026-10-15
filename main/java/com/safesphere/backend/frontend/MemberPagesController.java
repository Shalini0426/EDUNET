package com.safesphere.backend.frontend;

import com.safesphere.backend.account.UserAccountService;
import com.safesphere.backend.contact.EmergencyContactService;
import com.safesphere.backend.feedback.FeedbackService;
import com.safesphere.backend.chatbot.SafetyChatbotService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class MemberPagesController {

    private final UserAccountService accounts;
    private final EmergencyContactService contacts;
    private final FeedbackService feedback;
    private final SafetyChatbotService chatbot;

    public MemberPagesController(
            UserAccountService accounts,
            EmergencyContactService contacts,
            FeedbackService feedback,
            SafetyChatbotService chatbot) {
        this.accounts = accounts;
        this.contacts = contacts;
        this.feedback = feedback;
        this.chatbot = chatbot;
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        addDashboardData(authentication, model);
        return "features/dashboard";
    }

    @PostMapping("/chatbot")
    public String askChatbot(
            Authentication authentication,
            @RequestParam String question,
            Model model) {
        String prompt = question.trim();
        addDashboardData(authentication, model);
        model.addAttribute("chatQuestion", prompt);
        model.addAttribute("chatAnswer", chatbot.answer(prompt));
        return "features/dashboard";
    }

    private void addDashboardData(Authentication authentication, Model model) {
        model.addAttribute("account", accounts.requireByEmail(authentication.getName()));
        model.addAttribute("contactCount", contacts.findAll(authentication.getName()).size());
        model.addAttribute("averageRating", feedback.averageRating());
    }

    @GetMapping("/profile")
    public String profile(Authentication authentication, Model model) {
        model.addAttribute("account", accounts.requireByEmail(authentication.getName()));
        return "profile";
    }

    @PostMapping("/profile")
    public String updateProfile(
            Authentication authentication,
            @RequestParam String fullName,
            RedirectAttributes redirectAttributes) {
        accounts.updateProfile(authentication.getName(), fullName);
        redirectAttributes.addFlashAttribute("notice", "Your profile was updated.");
        return "redirect:/profile";
    }

    @GetMapping("/contacts")
    public String contacts(Authentication authentication, Model model) {
        model.addAttribute("contacts", contacts.findAll(authentication.getName()));
        return "contacts";
    }

    @PostMapping("/contacts")
    public String addContact(
            Authentication authentication,
            @RequestParam String name,
            @RequestParam String phoneNumber,
            @RequestParam(required = false) String relationship,
            RedirectAttributes redirectAttributes) {
        contacts.create(authentication.getName(), name, phoneNumber, relationship);
        redirectAttributes.addFlashAttribute("notice", "Emergency contact added.");
        return "redirect:/contacts";
    }

    @PostMapping("/contacts/{id}/delete")
    public String deleteContact(
            Authentication authentication,
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {
        contacts.delete(authentication.getName(), id);
        redirectAttributes.addFlashAttribute("notice", "Emergency contact removed.");
        return "redirect:/contacts";
    }

    @GetMapping("/feedback")
    public String feedback(Authentication authentication, Model model) {
        model.addAttribute("feedbackEntries", feedback.findAllForUser(authentication.getName()));
        model.addAttribute("averageRating", feedback.averageRating());
        return "feedback";
    }

    @PostMapping("/feedback")
    public String submitFeedback(
            Authentication authentication,
            @RequestParam int rating,
            @RequestParam String comments,
            RedirectAttributes redirectAttributes) {
        feedback.create(authentication.getName(), rating, comments);
        redirectAttributes.addFlashAttribute("notice", "Thank you for sharing your feedback.");
        return "redirect:/feedback";
    }

    @PostMapping("/feedback/{id}/delete")
    public String deleteFeedback(
            Authentication authentication,
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {
        feedback.delete(authentication.getName(), id, false);
        redirectAttributes.addFlashAttribute("notice", "Your feedback was deleted.");
        return "redirect:/feedback";
    }
}
