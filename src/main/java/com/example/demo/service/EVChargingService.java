package com.example.demo.service;

import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
public class EVChargingService {

    /**
     * Generate a Google Maps search URL to find EV charging stations near the given place name.
     * This method does NOT call any API but generates a reliable user-friendly URL.
     *
     * @param placeName Place name input by user (city, town, or village)
     * @return A message with a clickable Google Maps search URL
     */
    public String getChargingStationsSearchLink(String placeName) {
        try {
            // Encode the place name to make it URL safe
            String encodedPlace = URLEncoder.encode(placeName.trim(), StandardCharsets.UTF_8);
            // Build the Google Maps search URL for EV charging stations near the place
            String mapsLink = "https://www.google.com/maps/search/EV+charging+station+near+" + encodedPlace;

            // Return friendly message with link
            return "To find EV charging stations near " + placeName + ", just click:\n" + mapsLink;
        } catch (Exception e) {
            // Fallback error message if encoding fails (very unlikely)
            return "Sorry, something went wrong generating the charging station link.";
        }
    }
}
