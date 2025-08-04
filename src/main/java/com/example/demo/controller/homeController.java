package com.example.demo.controller;

import com.example.demo.service.GeminiSmartService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;

// Import or define geminiSmartService elsewhere
// e.g., import com.example.demo.service.GeminiSmartService;

@Controller
public class homeController {

    @Autowired
    private GeminiSmartService geminiSmartService;

    @Autowired
    public homeController(GeminiSmartService geminiSmartService) {
        this.geminiSmartService = geminiSmartService;
    }

    @GetMapping("/")
    public String homePage(Model model) {
        model.addAttribute("welcomeMessage", "Welcome to EV Voice Assistant");
        return "index";  // Thymeleaf template index.html will be rendered
    }

    // Use QueryRequest as the @RequestBody type for proper mapping
    @PostMapping("/api/voice-command")
    @ResponseBody
    public ResponseEntity<Map<String, String>> handleVoiceCommand(@RequestBody QueryRequest request) {
        String command = request.getText();
        String reply;

        System.out.println(command);

        if (command == null || command.trim().isEmpty()) {
            reply = "I didn't catch that, please try again.";
        } else {
            // Fallback to AI service if no known command matched
            reply = getAIResponseForFrontend(command);
        }

        Map<String, String> response = new HashMap<>();
        response.put("reply", reply);
        return ResponseEntity.ok(response);
    }

    private String getAIResponseForFrontend(String command) {
        // Call your AI service with the command text. Example:
        return geminiSmartService.handleQuery(command);
    }

    // Data class for request payload
    public static class QueryRequest {
        private String text;

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }
    }
}
