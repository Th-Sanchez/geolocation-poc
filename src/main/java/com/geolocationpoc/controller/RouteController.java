package com.geolocationpoc.controller;

import com.geolocationpoc.dto.Coordinate;
import com.geolocationpoc.dto.RouteRequest;
import com.geolocationpoc.dto.RouteResponse;
import com.geolocationpoc.dto.ServicePoint;
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
        return compositeService.optimizeBestRoute(request);
    }

    @PostMapping("/provider/{providerName}")
    public RouteResponse optimizeWithProvider(
            @PathVariable String providerName,
            @RequestBody RouteRequest request) {

        return compositeService.optimizeWithProvider(providerName, request);
    }

    // Helper endpoint for simple delivery scenarios
    @PostMapping("/delivery")
    public RouteResponse optimizeDelivery(@RequestBody SimpleDeliveryRequest request) {
        List<ServicePoint> services = request.deliveryPoints().stream()
                .map(point -> new ServicePoint(point.id(), point.location()))
                .toList();

        RouteRequest routeRequest = RouteRequest.simple(request.startLocation(), services);
        return compositeService.optimizeBestRoute(routeRequest);
    }

    public record SimpleDeliveryRequest(
            Coordinate startLocation,
            List<DeliveryPoint> deliveryPoints
    ) {}

    public record DeliveryPoint(String id, Coordinate location) {}
}
