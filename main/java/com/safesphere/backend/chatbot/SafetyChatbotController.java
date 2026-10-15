package com.safesphere.backend.chatbot;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chatbot")
public class SafetyChatbotController {

    private final SafetyChatbotService chatbot;

    public SafetyChatbotController(SafetyChatbotService chatbot) {
        this.chatbot = chatbot;
    }

    @PostMapping("/answer")
    public ChatbotResponse answer(@Valid @RequestBody ChatbotRequest request) {
        return new ChatbotResponse(chatbot.answer(request.question()));
    }

    public record ChatbotRequest(@NotBlank @Size(max = 500) String question) { }

    public record ChatbotResponse(String answer) { }
}