package com.example.demo.controller;

import com.example.demo.service.RouteService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RouteController {
    private final RouteService routeService;
    public RouteController(RouteService routeService) { this.routeService = routeService; }

    @GetMapping("/api/route")
    public String getRoute(@RequestParam String from, @RequestParam String to) {
        return routeService.getRouteSummary(from, to);
    }
}
