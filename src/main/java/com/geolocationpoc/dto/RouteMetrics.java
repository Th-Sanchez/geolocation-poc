package com.geolocationpoc.dto;

public record RouteMetrics(
        String vehicleId,
        Double distanceMeters,
        Long durationSeconds,
        Integer servicesPerformed,
        String startTime,
        String endTime
) {
}
