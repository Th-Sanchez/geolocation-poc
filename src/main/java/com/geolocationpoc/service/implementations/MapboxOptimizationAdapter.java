package com.geolocationpoc.service.implementations;

import com.fasterxml.jackson.databind.JsonNode;
import com.geolocationpoc.client.MapboxOptimizationClient;
import com.geolocationpoc.dto.Coordinate;
import com.geolocationpoc.dto.OptimizedStop;
import com.geolocationpoc.dto.RouteMetrics;
import com.geolocationpoc.dto.RouteRequest;
import com.geolocationpoc.dto.RouteResponse;
import com.geolocationpoc.service.RouteService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service("mapbox")
public class MapboxOptimizationAdapter implements RouteService {

    private final MapboxOptimizationClient client;

    public MapboxOptimizationAdapter(MapboxOptimizationClient client) {
        this.client = client;
    }

    @Override
    public RouteResponse optimizeRoute(RouteRequest request) {
        try {
            JsonNode response = client.optimizeRoutes(request);
            return parseMapboxResponse(response);

        } catch (Exception e) {
            throw new RuntimeException("Failed to optimize route with Mapbox", e);
        }
    }

    private RouteResponse parseMapboxResponse(JsonNode response) {
        List<OptimizedStop> allStops = new ArrayList<>();
        List<RouteMetrics> routeMetrics = new ArrayList<>();
        double totalDistance = 0;
        long totalDuration = 0;

        JsonNode routes = response.path("routes");
        for (JsonNode route : routes) {
            String vehicleId = route.path("vehicle").asText();
            JsonNode stops = route.path("stops");

            double routeDistance = 0;
            long routeDuration = 0;
            int serviceCount = 0;
            String startTime = null, endTime = null;

            OptimizedStop previousStop = null;

            for (JsonNode stop : stops) {
                String type = stop.path("type").asText();

                if (!"start".equals(type) && !"end".equals(type)) {
                    String serviceId = stop.path("location").asText();
                    String arrivalTime = stop.path("eta").asText();
                    int waitTime = stop.path("wait").asInt();
                    double odometer = stop.path("odometer").asDouble();

                    // Extract coordinates (would need reverse lookup)
                    Coordinate location = new Coordinate(0D, 0D);

                    double distanceFromPrev = previousStop != null ?
                            odometer - previousStop.distanceFromPrevious() : 0;

                    OptimizedStop optimizedStop = new OptimizedStop(
                            serviceId, location, arrivalTime, arrivalTime,
                            waitTime, distanceFromPrev, 0L
                    );
                    allStops.add(optimizedStop);
                    serviceCount++;
                    previousStop = optimizedStop;
                }

                if ("start".equals(type)) {
                    startTime = stop.path("eta").asText();
                } else if ("end".equals(type)) {
                    endTime = stop.path("eta").asText();
                    routeDistance = stop.path("odometer").asDouble();
                }
            }

            RouteMetrics routeMetric = new RouteMetrics(
                    vehicleId, routeDistance, routeDuration,
                    serviceCount, startTime, endTime
            );
            routeMetrics.add(routeMetric);

            totalDistance += routeDistance;
        }

        return new RouteResponse(totalDistance, totalDuration, allStops, routeMetrics, "Mapbox");
    }

    @Override
    public String providerName() {
        return "Mapbox";
    }

}
