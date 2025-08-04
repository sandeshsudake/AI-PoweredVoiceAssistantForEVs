package com.example.demo.controller;

import com.example.demo.service.GeminiSmartService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/gemini")
public class GeminiSmartController {

    private final GeminiSmartService geminiSmartService;

    public GeminiSmartController(GeminiSmartService geminiSmartService) {
        this.geminiSmartService = geminiSmartService;
    }

    /**
     * POST /api/gemini/smart
     * JSON body: { "text": "user query here" }
     * Returns the response text after NLU and dispatching.
     */
    @PostMapping("/smart")
    public String handleSmartQuery(@RequestBody QueryRequest request) {
        return geminiSmartService.handleQuery(request.getText());
    }

    public static class QueryRequest {
        private String text;

        public String getText() { return text; }
        public void setText(String text) { this.text = text; }
    }
}
