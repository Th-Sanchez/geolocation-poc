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

import com.google.auth.oauth2.GoogleCredentials;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.Duration;

public class GoogleOptimizationClient {

    private final WebClient client;
    private final ObjectMapper mapper = new ObjectMapper();
    private final String projectId;
    private final GoogleCredentials credentials;

    public GoogleOptimizationClient(WebClient.Builder builder, String projectId) {
        this.client = builder
                .baseUrl("https://routeoptimization.googleapis.com")
                .build();
        this.projectId = projectId;

        try {
            this.credentials = initializeCredentials();
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize Google credentials. " +
                    "Make sure GOOGLE_APPLICATION_CREDENTIALS is set to the path of your service account JSON file.", e);
        }
    }

    private GoogleCredentials initializeCredentials() throws IOException {
        String credentialsPath = System.getenv("GOOGLE_APPLICATION_CREDENTIALS");
        if (credentialsPath != null && !credentialsPath.isEmpty()) {
            System.out.println("Using service account from: " + credentialsPath);
            try (FileInputStream serviceAccount = new FileInputStream(credentialsPath)) {
                return GoogleCredentials.fromStream(serviceAccount)
                        .createScoped("https://www.googleapis.com/auth/cloud-platform");
            }
        }

        System.out.println("Trying Application Default Credentials...");
        return GoogleCredentials.getApplicationDefault()
                .createScoped("https://www.googleapis.com/auth/cloud-platform");
    }

    public JsonNode optimizeTours(RouteRequest request) {
        try {
            ObjectNode body = buildOptimizationRequest(request);

            return client.post()
                    .uri("/v1/projects/{projectId}:optimizeTours", projectId)
                    .header("Authorization", "Bearer " + getAccessToken())
                    .header("Content-Type", "application/json")
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block(Duration.ofSeconds(30));

        } catch (WebClientResponseException e) {
            throw new RuntimeException("Google Optimization API error: " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            throw new RuntimeException("Google Optimization API request failed", e);
        }
    }

    private ObjectNode buildOptimizationRequest(RouteRequest request) {
        ObjectNode root = mapper.createObjectNode();
        ObjectNode model = mapper.createObjectNode();

        // Build shipments (services as pickup+delivery pairs or just deliveries)
        ArrayNode shipments = mapper.createArrayNode();
        for (ServicePoint service : request.services()) {
            ObjectNode shipment = mapper.createObjectNode();

            // For simplicity, treat each service as a delivery
            ArrayNode deliveries = mapper.createArrayNode();
            ObjectNode delivery = mapper.createObjectNode();
            delivery.set("arrivalLocation", createLocationNode(service.location()));
            delivery.put("duration", service.durationSeconds() + "s");

            if (service.demandKg() > 0) {
                ObjectNode loadDemand = mapper.createObjectNode();
                loadDemand.put("amount", service.demandKg());
                delivery.set("loadDemands", mapper.createObjectNode().set("weight", loadDemand));
            }

            deliveries.add(delivery);
            shipment.set("deliveries", deliveries);
            shipment.put("label", service.id());

            shipments.add(shipment);
        }
        model.set("shipments", shipments);

        // Build vehicles
        ArrayNode vehicles = mapper.createArrayNode();
        for (Vehicle vehicle : request.vehicles()) {
            ObjectNode vehicleNode = mapper.createObjectNode();
            vehicleNode.put("label", vehicle.id());
            vehicleNode.set("startLocation", createLocationNode(vehicle.startLocation()));
            vehicleNode.set("endLocation", createLocationNode(vehicle.endLocation()));

            if (vehicle.capacityKg() > 0) {
                ObjectNode loadLimit = mapper.createObjectNode();
                loadLimit.put("maxLoad", vehicle.capacityKg());
                vehicleNode.set("loadLimits", mapper.createObjectNode().set("weight", loadLimit));
            }

            vehicles.add(vehicleNode);
        }
        model.set("vehicles", vehicles);

        // Time windows
        if (request.globalStartTime() != null) {
            model.put("globalStartTime", request.globalStartTime());
        }
        if (request.globalEndTime() != null) {
            model.put("globalEndTime", request.globalEndTime());
        }

        root.set("model", model);
        return root;
    }

    private ObjectNode createLocationNode(Coordinate coordinate) {
        ObjectNode location = mapper.createObjectNode();
        location.put("latitude", coordinate.lat());
        location.put("longitude", coordinate.lng());
        return location;
    }

    private String getAccessToken() {
        try {
            credentials.refreshIfExpired();
            String token = credentials.getAccessToken().getTokenValue();
            System.out.println("Successfully obtained access token: " + token.substring(0, 20) + "...");
            return token;
        } catch (IOException e) {
            System.err.println("Failed to get access token from credentials: " + e.getMessage());

            // Fallback para variável de ambiente (desenvolvimento)
            String envToken = System.getenv("GOOGLE_ACCESS_TOKEN");
            if (envToken != null && !envToken.isEmpty()) {
                System.out.println("Using token from GOOGLE_ACCESS_TOKEN environment variable");
                return envToken;
            }

            throw new RuntimeException("Failed to get Google access token. " +
                    "Please check your GOOGLE_APPLICATION_CREDENTIALS or run 'gcloud auth application-default login'", e);
        }
    }
}
