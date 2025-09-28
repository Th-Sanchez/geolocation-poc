package com.geolocationpoc.service;

import com.geolocationpoc.dto.RouteRequest;
import com.geolocationpoc.dto.RouteResponse;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class CompositeRouteService {

    private final List<RouteService> providers;
    private final ExecutorService executor = Executors.newFixedThreadPool(2);

    public CompositeRouteService(List<RouteService> providers) {
        this.providers = providers;
    }

    public RouteResponse optimizeBestRoute(RouteRequest request) {
        List<CompletableFuture<RouteResponse>> futures = providers.stream()
                .map(provider -> CompletableFuture.supplyAsync(() -> {
                    try {
                        long startTime = System.currentTimeMillis();
                        RouteResponse response = provider.optimizeRoute(request);
                        long endTime = System.currentTimeMillis();

                        System.out.printf("Provider %s took %dms%n",
                                provider.providerName(), (endTime - startTime));

                        return response;
                    } catch (Exception e) {
                        System.err.println("Provider " + provider.providerName() + " failed: " + e.getMessage());
                        return null;
                    }
                }, executor))
                .toList();

        return futures.stream()
                .map(CompletableFuture::join)
                .filter(Objects::nonNull)
                .min(Comparator.comparingDouble(RouteResponse::totalDistanceMeters))
                .orElseThrow(() -> new RuntimeException("No optimized routes available"));
    }

    public RouteResponse optimizeWithProvider(String providerName, RouteRequest request) {
        return providers.stream()
                .filter(provider -> provider.providerName().equalsIgnoreCase(providerName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Provider not found: " + providerName))
                .optimizeRoute(request);
    }
}
