package com.geolocationpoc.dto;

public record OptimizedStop(
        String serviceId,
        Coordinate location,
        String arrivalTime,
        String departureTime,
        Integer waitTimeSeconds,
        Double distanceFromPrevious,
        Long travelTimeFromPrevious
) {
}
