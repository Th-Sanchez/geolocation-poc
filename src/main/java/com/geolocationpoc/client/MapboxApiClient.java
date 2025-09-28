package com.geolocationpoc.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.geolocationpoc.dto.Coordinate;
import com.geolocationpoc.dto.RouteRequest;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

public class MapboxApiClient {

    private final WebClient webClient;
    private final String apiKey;

    public MapboxApiClient(WebClient.Builder builder, String apiKey) {
        this.webClient = builder
                .baseUrl("https://api.mapbox.com")
                .build();
        this.apiKey = apiKey;
    }

    public JsonNode getOptimizedRoute(RouteRequest request) {
        try {
            String profile = "driving";
            String coordinates = buildCoordinatesString(request.waypoints());

            return webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/optimized-trips/v1/mapbox/{profile}/{coordinates}")
                            .queryParam("geometries", "polyline")
                            .queryParam("steps", "false")
                            .queryParam("overview", "full")
                            .queryParam("source", request.roundTrip() ? "any" : "first")
                            .queryParam("destination", request.roundTrip() ? "any" : "last")
                            .queryParam("roundtrip", request.roundTrip())
                            .queryParam("access_token", apiKey)
                            .build(profile, coordinates))
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block(Duration.ofSeconds(15));
        } catch (WebClientResponseException e) {
            throw new RuntimeException("Mapbox API error: " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            throw new RuntimeException("Mapbox API request failed", e);
        }
    }

    private String buildCoordinatesString(List<Coordinate> waypoints) {
        return waypoints.stream()
                .map(coord -> String.format("%.6f,%.6f", coord.longitude(), coord.latitude()))
                .collect(Collectors.joining(";"));
    }
}
