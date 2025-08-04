package com.example.demo.controller;

import com.example.demo.service.SmartQueryService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class SmartQueryController {

    private final SmartQueryService smartQueryService;

    public SmartQueryController(SmartQueryService smartQueryService) {
        this.smartQueryService = smartQueryService;
    }

    /**
     * Unified endpoint to process user queries dynamically.
     * Supports both GET with 'text' query param and POST with JSON body containing 'text'.
     *
     * Examples:
     * GET /api/query?text=Show me weather in Pune
     * POST /api/query with JSON { "text": "Best route from Mumbai to Pune" }
     *
     * @param text user query text (from GET)
     * @return intelligent response string
     */
    @GetMapping("/query")
    public String handleGetQuery(@RequestParam("text") String text) {
        return smartQueryService.handleUserQuery(text);
    }

    @PostMapping("/query")
    public String handlePostQuery(@RequestBody Map<String, String> payload) {
        String text = payload.getOrDefault("text", "");
        return smartQueryService.handleUserQuery(text);
    }
}
