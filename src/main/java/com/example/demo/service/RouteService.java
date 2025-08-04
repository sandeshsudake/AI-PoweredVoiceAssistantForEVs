package com.example.demo.service;

import org.json.JSONArray;    // Helps handle arrays in JSON format
import org.json.JSONObject;   // Helps handle objects in JSON format
import org.springframework.stereotype.Service;  // Marks this class as a Service in Spring Boot
import org.springframework.web.client.RestTemplate;  // Used to make HTTP requests to other services

import java.net.URLEncoder;  // Helps to safely encode strings for URLs (replace spaces, special chars)
import java.nio.charset.StandardCharsets;  // Defines standard character encodings like UTF-8

@Service  // This tells Spring Boot that this class does some business logic and can be used in other parts of the app
public class RouteService {

    // Create a tool to send HTTP requests and get responses from websites or APIs
    private final RestTemplate restTemplate = new RestTemplate();

    // This method takes a place name like "Mumbai" and finds its GPS coordinates (longitude, latitude)
    public double[] geocodePlace(String placeName) {
        try {
            // Build a URL to ask the OpenStreetMap service called Nominatim for the place's location
            // We encode the place name so spaces and symbols don't break the web address
            String url = "https://nominatim.openstreetmap.org/search?q="
                    + URLEncoder.encode(placeName.trim(), StandardCharsets.UTF_8)
                    + "&format=json&limit=1";  // We ask for the response in JSON format and only 1 result

            // Send a GET request to that URL and store the response as a string (JSON data)
            String response = restTemplate.getForObject(url, String.class);

            // Parse the response text as a JSON array (list of results)
            JSONArray arr = new JSONArray(response);

            // If no results found, throw an error that the place was not found
            if (arr.length() == 0)
                throw new RuntimeException("Place not found: " + placeName);

            // Pick the first result from the list, it's the best guess
            JSONObject obj = arr.getJSONObject(0);

            // Extract the latitude and longitude values (they come as strings, so convert to double numbers)
            double lat = Double.parseDouble(obj.getString("lat"));
            double lon = Double.parseDouble(obj.getString("lon"));

            // Return the coordinates as a double array: longitude first, then latitude
            return new double[]{lon, lat};
        } catch (Exception e) {
            // If anything goes wrong, throw a clear message including place name and error details
            throw new RuntimeException("Geocoding failed for " + placeName + ": " + e.getMessage());
        }
    }

    // This method takes two place names and gets a simple route summary including time, distance, and a Google Maps link
    public String getRouteSummary(String from, String to) {
        try {
            // Find coordinates for the starting place
            double[] fromCoords = geocodePlace(from);

            // Find coordinates for the destination place
            double[] toCoords = geocodePlace(to);

            // Build the URL to call the OSRM routing service providing start and end coordinates
            // The format has longitude and latitude pairs separated by a semicolon
            String routeUrl = String.format(
                    "https://router.project-osrm.org/route/v1/driving/%.7f,%.7f;%.7f,%.7f?overview=false",
                    fromCoords[0], fromCoords[1], toCoords[0], toCoords[1]
            );

            // Send a GET request to the routing URL and get the JSON response as a string
            String response = restTemplate.getForObject(routeUrl, String.class);

            // Parse the response string into a JSON object so we can extract needed info
            JSONObject json = new JSONObject(response);

            // Extract the array of possible routes found (usually multiple possible routes)
            JSONArray routes = json.getJSONArray("routes");

            // If no route is found by OSRM, let user know we couldn't find a path
            if (routes.length() == 0)
                return "Sorry, I could not find a route between those locations.";

            // Use the first route from the list (the best or fastest one usually)
            JSONObject route = routes.getJSONObject(0);

            // Extract how long the trip will take in seconds and convert to hours (divide by 3600)
            double durationHrs = route.getDouble("duration") / 3600.0;

            // Extract how far the trip is in meters and convert to kilometers (divide by 1000)
            double distanceKm = route.getDouble("distance") / 1000.0;

            // Build a Google Maps link that the user can click to see the route on a map
            // Encode from and to place names safely for use in the URL
            String mapUrl = "https://www.google.com/maps/dir/"
                    + URLEncoder.encode(from, StandardCharsets.UTF_8)
                    + "/"
                    + URLEncoder.encode(to, StandardCharsets.UTF_8);

            // Return a nicely formatted string telling the user the distance, time, and a clickable link
            return String.format(
                    "The fastest route from %s to %s is %.1f km and should take about %.1f hours.\n[View on Google Maps](%s)",
                    from, to, distanceKm, durationHrs, mapUrl
            );
        } catch (Exception e) {
            // If anything goes wrong here, return a friendly error message to the user
            return "Sorry, I couldn't find one of the locations or route. Please try a more specific place name.";
        }
    }
}
