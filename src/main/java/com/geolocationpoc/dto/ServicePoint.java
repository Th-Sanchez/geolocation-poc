package com.geolocationpoc.dto;

public record ServicePoint(
        String id,
        Coordinate location,
        Integer durationSeconds,
        Integer demandKg) {

    public ServicePoint(String id, Coordinate location) {
        this(id, location, 300, 10);
    }

}
