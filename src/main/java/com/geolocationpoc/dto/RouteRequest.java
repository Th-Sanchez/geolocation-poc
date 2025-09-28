package com.geolocationpoc.dto;

import java.util.List;

public record RouteRequest(
        List<Coordinate> waypoints,
        String travelMode,
        Boolean optimizerOrder,
        Boolean roundTrip) {

    public Coordinate origin() {
        return waypoints.getFirst();
    }

    public Coordinate destination() {
        return waypoints.getLast();
    }

    public List<Coordinate> intermediates() {
        if (waypoints.size() <= 2) return List.of();
        return waypoints.subList(1, waypoints.size() - 1);
    }

}
