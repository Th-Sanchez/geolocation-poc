package com.geolocationpoc.config;

import com.geolocationpoc.client.GoogleOptimizationClient;
import com.geolocationpoc.client.MapboxOptimizationClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class RouteConfig {

    @Value("${google.projectId}")
    private String googleProjectId;

    @Value("${mapbox.token}")
    private String mapboxToken;

    @Bean
    public GoogleOptimizationClient googleApiClient(WebClient.Builder builder) {
        return new GoogleOptimizationClient(builder, googleProjectId);
    }

    @Bean
    public MapboxOptimizationClient mapboxApiClient(WebClient.Builder builder) {
        return new MapboxOptimizationClient(builder, mapboxToken);
    }

}
