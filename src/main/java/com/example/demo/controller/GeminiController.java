package com.example.demo.controller;

import com.example.demo.service.GeminiService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/gemini")
@RequiredArgsConstructor
public class GeminiController {

    private final GeminiService geminiService;

    @GetMapping("/ask")
    public String askGeminiAPI(@RequestBody String prompt){

        return geminiService.askGemini(prompt);
    }


}
