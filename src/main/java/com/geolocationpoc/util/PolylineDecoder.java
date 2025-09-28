package com.geolocationpoc.util;

import com.geolocationpoc.dto.Coordinate;

import java.util.ArrayList;
import java.util.List;

public class PolylineDecoder {

    public static List<Coordinate> decode(String encoded, Integer precision) {
        List<Coordinate> coordinates = new ArrayList<>();

        if (encoded == null || encoded.isEmpty()) {
            return coordinates;
        }

        Integer index = 0;
        Integer lat = 0;
        Integer lng = 0;
        Integer factor = (int) Math.pow(10, precision);

        while (index < encoded.length()) {
            Integer b;
            Integer shift = 0;
            Integer result = 0;

            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1F) << shift;
                shift += 5;
            } while (b >= 0x20);

            Integer deltaLet = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += deltaLet;

            shift = 0;
            result = 0;

            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1F) << shift;
                shift += 5;
            } while (b >= 0x20);

            Integer deltaLng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += deltaLng;

            coordinates.add(new Coordinate(
                    (double) lat / factor,
                    (double)lng / factor
            ));
        }

        return coordinates;
    }

}
