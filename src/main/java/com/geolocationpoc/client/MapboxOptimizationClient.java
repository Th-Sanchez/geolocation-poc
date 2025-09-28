package com.geolocationpoc.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.geolocationpoc.dto.Coordinate;
import com.geolocationpoc.dto.RouteRequest;
import com.geolocationpoc.dto.ServicePoint;
import com.geolocationpoc.dto.Vehicle;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;

public class MapboxOptimizationClient {

    private final WebClient client;
    private final ObjectMapper mapper = new ObjectMapper();
    private final String accessToken;

    public MapboxOptimizationClient(WebClient.Builder builder, String accessToken) {
        this.client = builder
                .baseUrl("https://api.mapbox.com")
                .build();
        this.accessToken = accessToken;
    }

    public JsonNode optimizeRoutes(RouteRequest request) {
        try {
            ObjectNode body = buildOptimizationRequest(request);

            return client.post()
                    .uri(uriBuilder -> uriBuilder
                            .path("/optimized-trips/v2")
                            .queryParam("access_token", accessToken)
                            .build())
                    .header("Content-Type", "application/json")
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block(Duration.ofSeconds(30));

        } catch (WebClientResponseException e) {
            throw new RuntimeException("Mapbox Optimization API error: " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            throw new RuntimeException("Mapbox Optimization API request failed", e);
        }
    }

    private ObjectNode buildOptimizationRequest(RouteRequest request) {
        ObjectNode root = mapper.createObjectNode();
        root.put("version", 1);

        // Build vehicles
        ArrayNode vehicles = mapper.createArrayNode();
        for (Vehicle vehicle : request.vehicles()) {
            ObjectNode vehicleNode = mapper.createObjectNode();
            vehicleNode.put("id", vehicle.id());

            // Start location
            ObjectNode startLoc = mapper.createObjectNode();
            startLoc.put("name", "start-" + vehicle.id());
            startLoc.put("coordinates", createCoordinateArray(vehicle.startLocation()));
            vehicleNode.set("start", startLoc);

            // End location
            ObjectNode endLoc = mapper.createObjectNode();
            endLoc.put("name", "end-" + vehicle.id());
            endLoc.put("coordinates", createCoordinateArray(vehicle.endLocation()));
            vehicleNode.set("end", endLoc);

            if (vehicle.capacityKg() > 0) {
                vehicleNode.put("capacity", vehicle.capacityKg());
            }

            vehicles.add(vehicleNode);
        }
        root.set("vehicles", vehicles);

        // Build services
        ArrayNode services = mapper.createArrayNode();
        for (ServicePoint service : request.services()) {
            ObjectNode serviceNode = mapper.createObjectNode();
            serviceNode.put("id", service.id());
            serviceNode.put("coordinates", createCoordinateArray(service.location()));
            serviceNode.put("duration", service.durationSeconds());

            if (service.demandKg() > 0) {
                serviceNode.put("size", service.demandKg());
            }

            services.add(serviceNode);
        }
        root.set("services", services);

        return root;
    }

    private ArrayNode createCoordinateArray(Coordinate coordinate) {
        ArrayNode coords = mapper.createArrayNode();
        coords.add(coordinate.lng()); // Mapbox uses [lng, lat]
        coords.add(coordinate.lat());
        return coords;
    }
}
