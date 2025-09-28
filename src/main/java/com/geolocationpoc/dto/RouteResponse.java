package com.geolocationpoc.dto;

import java.util.List;

public record RouteResponse(
        Double distanceMeters,
        Long durationSeconds,
        List<Coordinate> path,
        List<Integer> optimizedWaypointOrder,
        String provider) {
}
