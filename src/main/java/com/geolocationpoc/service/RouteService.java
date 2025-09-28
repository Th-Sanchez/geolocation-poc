package com.geolocationpoc.service;

import com.geolocationpoc.dto.RouteRequest;
import com.geolocationpoc.dto.RouteResponse;

public interface RouteService {
    RouteResponse calculateOptimizedRoute(RouteRequest request);
    String providerName();
}
