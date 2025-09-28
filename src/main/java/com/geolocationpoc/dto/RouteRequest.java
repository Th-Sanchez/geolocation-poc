package com.geolocationpoc.dto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public record RouteRequest(
        List<Vehicle> vehicles,
        List<ServicePoint> services,
        String globalStartTime,
        String globalEndTime,
        Boolean optimizerOrder) {

    public RouteRequest(List<Vehicle> vehicles, List<ServicePoint> services) {
        this(vehicles, services,
                LocalDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                LocalDateTime.now().plusHours(8).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                true);
    }

    public static RouteRequest simple(Coordinate startLocation, List<ServicePoint> services) {
        Vehicle vehicle = new Vehicle("vehicle-1", startLocation, startLocation);
        return new RouteRequest(List.of(vehicle), services);
    }

}
