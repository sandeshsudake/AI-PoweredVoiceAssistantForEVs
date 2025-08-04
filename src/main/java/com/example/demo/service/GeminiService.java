package com.example.demo.service;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GeminiService {

    private final Client client;


    public String askGemini(String prompt){

        GenerateContentResponse response =
                client.models.generateContent("gemini-2.0-flash-001", prompt, null);


        return response.text();
    }



}
