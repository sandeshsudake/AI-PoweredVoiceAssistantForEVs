package com.example.demo.service;

import com.example.demo.entity.IntentData;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class GeminiSmartService {

    private final GeminiService geminiService;
    private final WeatherService weatherService;
    private final GoogleMapsRouteService googleMapsRouteService;
    private final EVChargingService evChargingService;
    private final ObjectMapper objectMapper;

    public GeminiSmartService(GeminiService geminiService,
                              WeatherService weatherService,
                              GoogleMapsRouteService googleMapsRouteService,
                              EVChargingService evChargingService) {
        this.geminiService = geminiService;
        this.weatherService = weatherService;
        this.googleMapsRouteService = googleMapsRouteService;
        this.evChargingService = evChargingService;
        this.objectMapper = new ObjectMapper();
    }

    public String handleQuery(String userQuery) {
        try {
            String prompt = buildMultiIntentPrompt(userQuery);
            String rawResponse = geminiService.askGemini(prompt);
            rawResponse = cleanRawResponse(rawResponse);

            System.out.println("Gemini raw response: " + rawResponse);

            List<IntentData> intents = objectMapper.readValue(rawResponse, new TypeReference<List<IntentData>>() {});

            StringBuilder combinedResponse = new StringBuilder();

            boolean anyResponse = false;
            for (IntentData intent : intents) {
                String res = processSingleIntent(intent, userQuery);
                if (res != null && !res.isEmpty()) {
                    combinedResponse.append(res).append("\n\n");
                    anyResponse = true;
                }
            }

            if (!anyResponse) {
                return "Sorry, I couldn't process your request. Please try rephrasing.";
            }

            return combinedResponse.toString().trim();

        } catch (Exception e) {
            e.printStackTrace();
            return "An error occurred while processing your request.";
        }
    }

    private String processSingleIntent(IntentData intent, String userQuery) {
        if (intent == null) return null;

        String intentName = intent.getIntent();
        if (intentName == null || intentName.isBlank()) return null;

        intentName = intentName.trim().toLowerCase();

        switch (intentName) {
            case "weather":
                if (isNullOrEmpty(intent.getPlace())) {
                    return "Please specify the location for the weather information.";
                }
                return weatherService.getCurrentWeather(intent.getPlace());

            case "route":
                if (isNullOrEmpty(intent.getFromPlace()) || isNullOrEmpty(intent.getToPlace())) {
                    return "Please specify both origin and destination for the route.";
                }
                return googleMapsRouteService.getGoogleMapsRouteLink(intent.getFromPlace(), intent.getToPlace());

            case "charging":
                if (isNullOrEmpty(intent.getPlace())) {
                    return "Please specify the location to find nearby charging stations.";
                }
                return generatePoiSearchResponse("charging station", intent.getPlace());

            case "hotel":
                if (isNullOrEmpty(intent.getPlace())) {
                    return "Please specify the location to find hotels.";
                }
                return generatePoiSearchResponse("hotel", intent.getPlace());

            case "poi_search":
                if (isNullOrEmpty(intent.getPoiType())) {
                    return "Please specify what type of place you're looking for.";
                }
                String place = intent.getPlace() != null ? intent.getPlace() : "";
                return generatePoiSearchResponse(intent.getPoiType(), place);

            case "media_play":
                if (!isNullOrEmpty(intent.getResponse())) {
                    return intent.getResponse();
                } else {
                    return "Playing your requested media shortly.";
                }

            case "general":
                if (!isNullOrEmpty(intent.getResponse())) {
                    return intent.getResponse();
                } else {
                    try {
                        String fallback = geminiService.askGemini("Answer concisely:\n" + userQuery);
                        return isNullOrEmpty(fallback) ? "Sorry, I don't have an answer for that." : fallback.trim();
                    } catch (Exception e) {
                        return "I had trouble generating a response. Please try again.";
                    }
                }

            default:
                return null;
        }
    }

    private String generatePoiSearchResponse(String poiType, String place) {
        try {
            String query = poiType.trim();
            if (!place.isBlank()) {
                query += " near " + place.trim();
            }
            String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
            String mapsUrl = "https://www.google.com/maps/search/" + encodedQuery;

            return "Here are some " + query + ":\n" + mapsUrl;
        } catch (Exception e) {
            return "Sorry, I couldn't generate a map link for your request.";
        }
    }

    private boolean isNullOrEmpty(String s) {
        return s == null || s.isBlank();
    }

    private String buildMultiIntentPrompt(String userQuery) {
        return "You are a smart assistant. Analyze the user query exactly: \"" + userQuery + "\".\n"
                + "Extract ALL intents present, and return ONLY a JSON array of objects with these fields:\n"
                + "{\n"
                + "  \"intent\": \"weather|route|charging|hotel|poi_search|media_play|general\",\n"
                + "  \"place\": \"<location name or null>\",\n"
                + "  \"fromPlace\": \"<origin or null>\",\n"
                + "  \"toPlace\": \"<destination or null>\",\n"
                + "  \"poiType\": \"<type of point of interest or null>\",\n"
                + "  \"response\": \"<free-form answer text or null>\"\n"
                + "}\n"
                + "For example:\n"
                + "[\n"
                + "  {\"intent\":\"poi_search\", \"poiType\":\"coffee shop\", \"place\":\"Pune\", \"fromPlace\":null, \"toPlace\":null, \"response\":null},\n"
                + "  {\"intent\":\"general\", \"response\":\"Coffee shops sell coffee beverages.\", \"place\":null, \"fromPlace\":null, \"toPlace\":null, \"poiType\":null}\n"
                + "]\n"
                + "Do not add any explanation or markdown formatting. Only raw JSON array.";
    }

    private String cleanRawResponse(String rawResponse) {
        String cleaned = rawResponse.trim();

        if (cleaned.startsWith("```json")) {
            cleaned = cleaned.substring(7).trim(); // remove ```json
        } else if (cleaned.startsWith("```")) {
            cleaned = cleaned.substring(3).trim(); // remove ```
        }

        if (cleaned.endsWith("```")) {
            cleaned = cleaned.substring(0, cleaned.length() - 3).trim(); // remove ending ```
        }

        return cleaned;
    }

}
