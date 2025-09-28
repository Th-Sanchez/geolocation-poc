package com.geolocationpoc.dto;

public record Vehicle(
        String id,
        Coordinate startLocation,
        Coordinate endLocation,
        String startTime,
        String endTime,
        Integer capacityKg){

    public Vehicle(String id, Coordinate startLocation, Coordinate endLocation) {
        this(id, startLocation, endLocation, null, null, 1000);
    }

}
