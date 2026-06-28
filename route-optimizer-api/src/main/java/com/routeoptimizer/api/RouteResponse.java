package com.routeoptimizer.api;

import com.routeoptimizer.graph.Node;
import java.util.List;

/** JSON response shape for a single route */
public record RouteResponse(
    List<Stop> path,
    double totalDistanceKm,
    int nodesExplored,
    String algorithm
) {
    public record Stop(String id, String name, double lat, double lng) {
        public static Stop from(Node n) {
            return new Stop(n.id, n.name, n.lat, n.lng);
        }
    }
}
