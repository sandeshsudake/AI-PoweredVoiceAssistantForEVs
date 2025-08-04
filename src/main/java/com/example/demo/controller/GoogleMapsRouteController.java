package com.example.demo.controller;

import com.example.demo.service.GoogleMapsRouteService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GoogleMapsRouteController {

    private final GoogleMapsRouteService googleMapsRouteService;

    public GoogleMapsRouteController(GoogleMapsRouteService googleMapsRouteService) {
        this.googleMapsRouteService = googleMapsRouteService;
    }

    /**
     * API endpoint:  
     * GET /api/googlemaps/route?from=PLACE1&to=PLACE2
     *
     * Returns a user-friendly message including a Google Maps directions link.
     *
     * Example: /api/googlemaps/route?from=Mumbai&to=Pune
     */
    @GetMapping("/api/googlemaps/route")
    public String getRoute(
            @RequestParam String from,
            @RequestParam String to
    ) {
        return googleMapsRouteService.getGoogleMapsRouteLink(from, to);
    }
}
