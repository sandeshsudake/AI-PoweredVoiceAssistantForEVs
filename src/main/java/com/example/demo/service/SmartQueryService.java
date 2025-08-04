package com.example.demo.service;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class SmartQueryService {

    private final WeatherService weatherService;
    private final EVChargingService evChargingService;
    private final GoogleMapsRouteService googleMapsRouteService;

    public SmartQueryService(WeatherService weatherService,
                             EVChargingService evChargingService,
                             GoogleMapsRouteService googleMapsRouteService) {
        this.weatherService = weatherService;
        this.evChargingService = evChargingService;
        this.googleMapsRouteService = googleMapsRouteService;
    }

    /**
     * Processes the user query text and dispatches to the corresponding service.
     * Uses simple keyword matching with improved extraction.
     *
     * @param userQuery free text user input
     * @return response string from the best matched service
     */
    public String handleUserQuery(String userQuery) {
        if (!StringUtils.hasText(userQuery)) {
            return "Please provide a valid query.";
        }

        String query = userQuery.toLowerCase();

        try {
            if (query.contains("weather")) {
                // Extract place name more robustly for weather queries
                String place = extractWeatherPlace(query);
                if (!StringUtils.hasText(place)) {
                    return "Please specify the place for which you want the weather.";
                }
                return weatherService.getCurrentWeather(place);

            } else if (query.contains("charge") || query.contains("ev station") || query.contains("charging station")) {
                // Extract place after 'near' or fallback to last word
                String place = extractPlaceNear(query);
                if (!StringUtils.hasText(place)) {
                    return "Please specify the location near which you want to find EV charging stations.";
                }
                return evChargingService.getChargingStationsSearchLink(place);

            } else if (query.contains("route") || query.contains("directions") || query.contains("way") || (query.contains("from") && query.contains("to"))) {
                String[] places = extractFromTo(query);
                if (places == null || places.length != 2) {
                    return "Please specify both the starting location and the destination for route directions.";
                }
                return googleMapsRouteService.getGoogleMapsRouteLink(places[0], places[1]);

            } else {
                return "Sorry, I didn't understand your request. You can ask about weather, best routes, or EV charging stations.";
            }
        } catch (Exception e) {
            return "Sorry, there was an error processing your request: " + e.getMessage();
        }
    }

    // Helper to extract place name for weather queries
    private String extractWeatherPlace(String input) {
        // Convert to lowercase for consistent replacements
        String cleaned = input.toLowerCase();

        // Remove common filler or command words
        cleaned = cleaned.replaceAll("show me", "")
                .replaceAll("tell me", "")
                .replaceAll("what is", "")
                .replaceAll("what's", "")
                .replaceAll("weather", "")
                .replaceAll("in", "")
                .replaceAll("at", "")
                .replaceAll("for", "")
                .replaceAll("\\?", "")
                .trim();

        return cleaned;
    }


    // Helper to extract place name after keyword 'near' or fallback
    private String extractPlaceNear(String text) {
        int idx = text.indexOf("near");
        if (idx != -1) {
            String place = text.substring(idx + 4).trim();
            if (!place.isEmpty()) return place;
        }
        String[] parts = text.split(" ");
        return parts.length > 0 ? parts[parts.length -1].trim() : "";
    }

    // Extract 'from' and 'to' place names for route queries like: "route from Mumbai to Pune"
    private String[] extractFromTo(String text) {
        try {
            int fromIdx = text.indexOf("from");
            int toIdx = text.indexOf("to");
            if (fromIdx == -1 || toIdx == -1 || toIdx <= fromIdx) {
                return null;
            }
            String fromPlace = text.substring(fromIdx + 4, toIdx).trim();
            String toPlace = text.substring(toIdx + 2).trim();

            if (fromPlace.isEmpty() || toPlace.isEmpty()) {
                return null;
            }
            return new String[] {fromPlace, toPlace};
        } catch (Exception e) {
            return null;
        }
    }
}
