package com.geolocationpoc.controller;

import com.geolocationpoc.dto.Coordinate;
import com.geolocationpoc.dto.RouteRequest;
import com.geolocationpoc.dto.RouteResponse;
import com.geolocationpoc.service.CompositeRouteService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/routes")
public class RouteController {

    private final CompositeRouteService compositeService;

    public RouteController(CompositeRouteService compositeService) {
        this.compositeService = compositeService;
    }

    @PostMapping("/optimize")
    public RouteResponse optimizeRoute(@RequestBody RouteRequest request) {
        return compositeService.calculateBestRoute(request);
    }

    @PostMapping("/provider/{providerName}")
    public RouteResponse optimizeWithProvider(
            @PathVariable String providerName,
            @RequestBody RouteRequest request) {

        return compositeService.calculateWithProvider(providerName, request);
    }

    @PostMapping("/compare")
    public List<RouteResponse> compareProviders(@RequestBody RouteRequest request) {
        return compositeService.compareAllProviders(request);
    }

    @GetMapping("/simple")
    public RouteResponse getSimpleRoute(
            @RequestParam double origemLat,
            @RequestParam double origemLng,
            @RequestParam double destinoLat,
            @RequestParam double destinoLng,
            @RequestParam(defaultValue = "false") boolean optimizeOrder) {

        List<Coordinate> waypoints = List.of(
                new Coordinate(origemLat, origemLng),
                new Coordinate(destinoLat, destinoLng)
        );

        RouteRequest request = new RouteRequest(waypoints, "DRIVE", optimizeOrder, false);
        return compositeService.calculateBestRoute(request);
    }
}
