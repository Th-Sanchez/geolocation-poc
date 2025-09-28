package com.geolocationpoc.config;

import com.geolocationpoc.client.GoogleApiClient;
import com.geolocationpoc.client.MapboxApiClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class RouteConfig {

    @Value("${api.google.key}")
    private String googleApiKey;

    @Value("${api.mapbox.key}")
    private String mapboxApiKey;

    @Bean
    public GoogleApiClient googleApiClient(WebClient.Builder builder) {
        return new GoogleApiClient(builder, googleApiKey);
    }

    @Bean
    public MapboxApiClient mapboxApiClient(WebClient.Builder builder) {
        return new MapboxApiClient(builder, mapboxApiKey);
    }

}
