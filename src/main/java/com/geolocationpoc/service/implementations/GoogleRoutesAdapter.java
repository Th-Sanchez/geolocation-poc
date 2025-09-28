package com.geolocationpoc.service.implementations;

import com.fasterxml.jackson.databind.JsonNode;
import com.geolocationpoc.client.GoogleApiClient;
import com.geolocationpoc.dto.Coordinate;
import com.geolocationpoc.dto.RouteRequest;
import com.geolocationpoc.dto.RouteResponse;
import com.geolocationpoc.service.RouteService;
import com.geolocationpoc.util.PolylineDecoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service("google")
public class GoogleRoutesAdapter implements RouteService {

    private final GoogleApiClient client;

    public GoogleRoutesAdapter(GoogleApiClient client) {
        this.client = client;
    }

    @Override
    public RouteResponse calculateOptimizedRoute(RouteRequest request) {
        try {
            JsonNode response = client.computeOptimizedRoute(request);
            JsonNode route = response.path("routes").get(0);

            double distance = route.path("distanceMeters").asDouble();
            String durStr = route.path("duration").asText();
            long durationSeconds = parseDuration(durStr);
            String encoded = route.path("polyline").path("encodedPolyline").asText();

            List<Coordinate> path = PolylineDecoder.decode(encoded, 5);
            List<Integer> optimizedOrder = extractOptimizedOrder(route);

            return new RouteResponse(distance, durationSeconds, path, optimizedOrder, "Google");

        } catch (Exception e) {
            throw new RuntimeException("Failed to calculate optimized route with Google", e);
        }
    }

    private Long parseDuration(String duration) {
        if (duration == null || duration.isEmpty()) {
            return 0L;
        }
        if (duration.endsWith("s")) {
            duration = duration.substring(0, duration.length() - 1);
        }
        try {
            return Long.parseLong(duration);
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    private List<Integer> extractOptimizedOrder(JsonNode route) {
        List<Integer> optimizedOrder = new ArrayList<>();
        JsonNode optimizedIndices = route.path("optimizedIntermediateWaypointIndex");

        if (optimizedIndices.isArray()) {
            for (JsonNode index : optimizedIndices) {
                optimizedOrder.add(index.asInt());
            }
        }

        return optimizedOrder;
    }

    @Override
    public String providerName() {
        return "Google";
    }
}
