package com.safesphere.backend.chatbot;

import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class SafetyChatbotService {

    public String answer(String question) {
        String prompt = question.toLowerCase(Locale.ROOT);
        if (containsAny(prompt, "emergency", "danger", "sos", "urgent")) {
            return "This chat cannot contact emergency services. If you are in immediate danger, call your local emergency number or contact someone you trust directly.";
        }
        if (containsAny(prompt, "journey", "route", "travel", "trip")) {
            return "Before travelling, choose a route you are comfortable with, share your plans only with trusted people, and agree on a check-in time. Route estimates in this app are not a safety guarantee.";
        }
        if (containsAny(prompt, "privacy", "location", "share", "data")) {
            return "Review your privacy settings before sharing location information. Share only what is needed, with people you trust, and turn sharing off when it is no longer needed.";
        }
        if (containsAny(prompt, "report", "incident", "category")) {
            return "Use the incident report form to select the closest category, describe the concern factually, and give a general area rather than a private address. Reports are for review, not emergency dispatch.";
        }
        if (containsAny(prompt, "contact", "trusted circle")) {
            return "You can manage trusted people from Emergency contacts. Confirm their details and ask for consent before relying on them for check-ins.";
        }
        if (containsAny(prompt, "rating", "feedback", "review")) {
            return "Open Feedback to leave a 1-to-5-star rating and comments. Your submissions are saved to your account and can be reviewed or deleted there.";
        }
        return "I can help explain incident reports, categories, feedback, privacy choices, trusted contacts, and journey planning. This guidance bot is not an emergency service.";
    }

    private boolean containsAny(String prompt, String... terms) {
        for (String term : terms) {
            if (prompt.contains(term)) {
                return true;
            }
        }
        return false;
    }
}