package com.example.demo.service;

import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
public class GoogleMapsRouteService {

    /**
     * Generates a Google Maps URL showing the best route from one place to another.
     * This link opens Google Maps with real-time, traffic-aware routing.
     *
     * @param fromPlace Origin place name (user input)
     * @param toPlace   Destination place name (user input)
     * @return A user-friendly message containing the clickable Google Maps directions URL
     */
    public String getGoogleMapsRouteLink(String fromPlace, String toPlace) {
        try {
            String fromEncoded = URLEncoder.encode(fromPlace.trim(), StandardCharsets.UTF_8);
            String toEncoded = URLEncoder.encode(toPlace.trim(), StandardCharsets.UTF_8);

            String mapUrl = "https://www.google.com/maps/dir/" + fromEncoded + "/" + toEncoded;

            return "To see the best route and live traffic updates from " + fromPlace + " to " + toPlace +
                    ", click here:\n" + mapUrl;

        } catch (Exception e) {
            return "Sorry, could not generate directions link.";
        }
    }
}
