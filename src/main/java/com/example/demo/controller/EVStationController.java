package com.example.demo.controller;

import com.example.demo.service.EVChargingService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EVStationController {

    private final EVChargingService evChargingService;

    public EVStationController(EVChargingService evChargingService) {
        this.evChargingService = evChargingService;
    }

    /**
     * Endpoint: GET /api/evstations?near=PLACE_NAME
     * Returns a Google Maps search link to show EV charging stations near user input place.
     *
     * @param placeName Name of the place near which to find charging stations
     * @return Message with Google Maps search URL
     */
    @GetMapping("/api/evstations")
    public String getChargingStations(@RequestParam("near") String placeName) {
        return evChargingService.getChargingStationsSearchLink(placeName);
    }
}
