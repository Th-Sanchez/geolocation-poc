package com.geolocationpoc.dto;

import java.util.List;

public record RouteResponse(
        Double totalDistanceMeters,
        Long totalDurationSeconds,
        List<OptimizedStop> stops,
        List<RouteMetrics> routeMetrics,
        String provider) {
}
