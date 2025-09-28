package com.geolocationpoc.service.implementations;

import com.fasterxml.jackson.databind.JsonNode;
import com.geolocationpoc.client.MapboxApiClient;
import com.geolocationpoc.dto.Coordinate;
import com.geolocationpoc.dto.RouteRequest;
import com.geolocationpoc.dto.RouteResponse;
import com.geolocationpoc.service.RouteService;
import com.geolocationpoc.util.PolylineDecoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service("mapbox")
public class MapboxRoutesAdapter implements RouteService {

    private final MapboxApiClient client;

    public MapboxRoutesAdapter(MapboxApiClient client) {
        this.client = client;
    }

    @Override
    public RouteResponse calculateOptimizedRoute(RouteRequest request) {
        try {
            JsonNode response = client.getOptimizedRoute(request);
            JsonNode trip = response.path("trips").get(0);

            double distance = trip.path("distance").asDouble();
            long durationSeconds = Math.round(trip.path("duration").asDouble());
            String encoded = trip.path("geometry").asText();

            List<Coordinate> path = PolylineDecoder.decode(encoded, 5);
            List<Integer> optimizedOrder = extractOptimizedOrder(response);

            return new RouteResponse(distance, durationSeconds, path, optimizedOrder, "Mapbox");

        } catch (Exception e) {
            throw new RuntimeException("Failed to calculate optimized route with Mapbox", e);
        }
    }

    private List<Integer> extractOptimizedOrder(JsonNode response) {
        List<Integer> optimizedOrder = new ArrayList<>();
        JsonNode waypoints = response.path("waypoints");

        if (waypoints.isArray()) {
            for (JsonNode waypoint : waypoints) {
                optimizedOrder.add(waypoint.path("waypoint_index").asInt());
            }
        }

        return optimizedOrder;
    }

    @Override
    public String providerName() {
        return "Mapbox";
    }

}
