package com.geolocationpoc.service.implementations;

import com.fasterxml.jackson.databind.JsonNode;
import com.geolocationpoc.client.GoogleOptimizationClient;
import com.geolocationpoc.dto.Coordinate;
import com.geolocationpoc.dto.OptimizedStop;
import com.geolocationpoc.dto.RouteMetrics;
import com.geolocationpoc.dto.RouteRequest;
import com.geolocationpoc.dto.RouteResponse;
import com.geolocationpoc.service.RouteService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service("googleOptimization")
public class GoogleOptimizationAdapter implements RouteService {

    private final GoogleOptimizationClient client;

    public GoogleOptimizationAdapter(GoogleOptimizationClient client) {
        this.client = client;
    }

    @Override
    public RouteResponse optimizeRoute(RouteRequest request) {
        try {
            JsonNode response = client.optimizeTours(request);
            return parseGoogleResponse(response);

        } catch (Exception e) {
            throw new RuntimeException("Failed to optimize route with Google", e);
        }
    }

    private RouteResponse parseGoogleResponse(JsonNode response) {
        List<OptimizedStop> allStops = new ArrayList<>();
        List<RouteMetrics> routeMetrics = new ArrayList<>();
        double totalDistance = 0;
        long totalDuration = 0;

        JsonNode routes = response.path("routes");
        for (JsonNode route : routes) {
            String vehicleId = route.path("vehicleLabel").asText("vehicle-1");

            // Parse visits and transitions
            JsonNode visits = route.path("visits");
            JsonNode transitions = route.path("transitions");

            for (int i = 0; i < visits.size(); i++) {
                JsonNode visit = visits.get(i);
                JsonNode transition = i < transitions.size() ? transitions.get(i) : null;

                String serviceId = visit.path("shipmentLabel").asText();
                String arrivalTime = visit.path("startTime").asText();

                // Extract location from original request or response
                Coordinate location = new Coordinate(0D, 0D); // Would need to map back from shipment

                double distanceFromPrev = 0;
                long travelTimeFromPrev = 0;

                if (transition != null) {
                    distanceFromPrev = transition.path("travelDistanceMeters").asDouble();
                    String travelDur = transition.path("travelDuration").asText();
                    travelTimeFromPrev = parseDurationString(travelDur);
                }

                OptimizedStop stop = new OptimizedStop(
                        serviceId, location, arrivalTime, arrivalTime,
                        0, distanceFromPrev, travelTimeFromPrev
                );
                allStops.add(stop);
            }

            // Parse route metrics
            JsonNode metrics = route.path("metrics");
            double routeDistance = metrics.path("travelDistanceMeters").asDouble();
            long routeDuration = parseDurationString(metrics.path("travelDuration").asText());

            RouteMetrics routeMetric = new RouteMetrics(
                    vehicleId, routeDistance, routeDuration,
                    metrics.path("performedShipmentCount").asInt(),
                    route.path("vehicleStartTime").asText(),
                    route.path("vehicleEndTime").asText()
            );
            routeMetrics.add(routeMetric);

            totalDistance += routeDistance;
            totalDuration += routeDuration;
        }

        return new RouteResponse(totalDistance, totalDuration, allStops, routeMetrics, "Google");
    }

    private long parseDurationString(String duration) {
        if (duration == null || duration.isEmpty()) return 0;
        if (duration.endsWith("s")) {
            return Long.parseLong(duration.substring(0, duration.length() - 1));
        }
        return 0;
    }

    @Override
    public String providerName() {
        return "Google";
    }
}
