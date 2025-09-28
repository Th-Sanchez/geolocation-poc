package com.geolocationpoc.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.geolocationpoc.dto.Coordinate;
import com.geolocationpoc.dto.RouteRequest;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;

public class GoogleApiClient {

    private final WebClient webClient;
    private final ObjectMapper mapper = new ObjectMapper();
    private final String apiKey;

    public GoogleApiClient(WebClient.Builder builder, String apiKey) {
        this.webClient = builder
                .baseUrl("https://routeoptimization.googleapis.com")
                .build();
        this.apiKey = apiKey;
    }

    public JsonNode computeOptimizedRoute(RouteRequest request) {
        try {
            ObjectNode body = buildRequestBody(request);

            return webClient.post()
                    .uri("/directions/v2:computeRoutes")
                    .header("X-Goog-Api-Key", apiKey)
                    .header("X-Goog-FieldMask",
                            "routes.distanceMeters,routes.duration,routes.polyline.encodedPolyline,routes.optimizedIntermediateWaypointIndex")
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block(Duration.ofSeconds(15));
        } catch (WebClientResponseException e) {
            throw new RuntimeException("Google API error: " + e.getResponseBodyAsString(), e);
        } catch(Exception e) {
            throw new RuntimeException("Google API request failed", e);
        }
    }

    private ObjectNode buildRequestBody(RouteRequest request) {
        ObjectNode body = mapper.createObjectNode();

        body.set("origin", createLocationNode(request.origin()));
        body.set("destination", createLocationNode(request.destination()));

        if (!request.intermediates().isEmpty()) {
            ArrayNode intermediates = mapper.createArrayNode();
            for (Coordinate waypoint : request.intermediates()) {
                intermediates.add(createLocationNode(waypoint));
            }
            body.set("intermediates", intermediates);
        }

        body.put("travelMode", "DRIVE");

        body.put("optimizedWaypointOrder", request.optimizerOrder());
        body.put("rountingPreference", "TRAFFIC_AWARE_OPTIMAL");

        return body;
    }

    private ObjectNode createLocationNode(Coordinate coordinate) {
        return mapper.createObjectNode()
                .set("location", mapper.createObjectNode()
                        .set("latLng", mapper.createObjectNode()
                                .put("latitude", coordinate.latitude())
                                .put("longitude", coordinate.longitude())));
    }
}
