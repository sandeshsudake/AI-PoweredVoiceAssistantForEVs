package com.example.demo.service;

import org.json.JSONObject;
import org.json.JSONArray;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
public class WeatherService {

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Get the real-time current weather for a place name using Open-Meteo free API.
     * Geocodes the place to get coordinates, then fetches weather info.
     *
     * @param placeName The name of the place (city, village, etc.)
     * @return A friendly weather summary string
     */
    public String getCurrentWeather(String placeName) {
        try {
            // Step 1: Geocode the place name to latitude and longitude
            double[] coords = geocodePlace(placeName); // returns [lon, lat]

            // Step 2: Build Open-Meteo API URL (note: latitude first, then longitude)
            String url = String.format(
                "https://api.open-meteo.com/v1/forecast?latitude=%.5f&longitude=%.5f&current_weather=true",
                coords[1], coords[0]
            );

            // Step 3: Call Open-Meteo API and get the response as JSON string
            String response = restTemplate.getForObject(url, String.class);

            // Step 4: Parse JSON and extract current weather data
            JSONObject json = new JSONObject(response);
            JSONObject current = json.optJSONObject("current_weather");

            if (current == null) {
                return "Sorry, I couldn't get the current weather data for " + placeName + ".";
            }

            double temperature = current.getDouble("temperature");   // Celsius
            double windspeed = current.getDouble("windspeed");       // km/h
            int weathercode = current.getInt("weathercode");         // Weather condition code

            String condition = weatherCodeToDescription(weathercode);

            // Step 5: Format and return friendly weather summary
            return String.format(
                "The current weather in %s is %s with a temperature of %.1f°C and wind speed of %.1f km/h.",
                placeName, condition, temperature, windspeed
            );

        } catch (Exception e) {
            return "Sorry, I couldn't get the weather for \"" + placeName + "\". Please try again.";
        }
    }

    /**
     * Simple Nominatim geocode method to get coordinates for a place name
     * Returns longitude, latitude
     */
    private double[] geocodePlace(String placeName) throws Exception {
        String url = "https://nominatim.openstreetmap.org/search?q="
            + URLEncoder.encode(placeName.trim(), StandardCharsets.UTF_8)
            + "&format=json&limit=1";

        String response = restTemplate.getForObject(url, String.class);
        JSONArray arr = new JSONArray(response);

        if (arr.length() == 0) {
            throw new Exception("Place not found: " + placeName);
        }

        JSONObject first = arr.getJSONObject(0);
        double lat = Double.parseDouble(first.getString("lat"));
        double lon = Double.parseDouble(first.getString("lon"));

        return new double[]{lon, lat};
    }

    /**
     * Converts Open-Meteo weather code to human-readable description.
     * Source: https://open-meteo.com/en/docs#latitude=52.52&longitude=13.41&current_weather=true
     */
    private String weatherCodeToDescription(int code) {
        switch (code) {
            case 0: return "clear skies";
            case 1:
            case 2:
            case 3:
                return "partly cloudy";
            case 45:
            case 48:
                return "foggy";
            case 51:
            case 53:
            case 55:
                return "light drizzle";
            case 61:
            case 63:
            case 65:
                return "rainy";
            case 71:
            case 73:
            case 75:
                return "snowy";
            case 80:
            case 81:
            case 82:
                return "showers";
            case 95:
            case 96:
            case 99:
                return "thunderstorms";
            default:
                return "good weather";
        }
    }
}
