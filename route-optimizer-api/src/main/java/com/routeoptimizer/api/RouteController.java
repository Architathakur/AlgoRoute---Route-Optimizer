package com.routeoptimizer.api;

import com.routeoptimizer.algorithms.AStar;
import com.routeoptimizer.algorithms.Dijkstra;
import com.routeoptimizer.algorithms.YensKShortest;
import com.routeoptimizer.graph.Graph;
import com.routeoptimizer.graph.Node;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * REST API for the Smart City Route Optimizer.
 *
 * Endpoints:
 *   GET /api/route?from=CP&to=IGI&algo=dijkstra     → single shortest route
 *   GET /api/route?from=CP&to=IGI&algo=astar        → single shortest route via A*
 *   GET /api/route/k?from=CP&to=IGI&k=3             → top-K shortest routes (Yen's)
 *   GET /api/route/benchmark?from=CP&to=IGI         → Dijkstra vs A* comparison
 *   GET /api/locations                               → list all city nodes
 *   POST /api/road/close?from=CP&to=INA&closed=true → toggle road closure
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")  // allow frontend dev servers to call this API
public class RouteController {

    private final GraphService graphService;

    public RouteController(GraphService graphService) {
        this.graphService = graphService;
    }

    /** List all available locations in the city */
    @GetMapping("/locations")
    public List<Map<String, Object>> getLocations() {
        return graphService.getGraph().getAllNodes().stream()
            .sorted(Comparator.comparing(n -> n.id))
            .map(n -> {
                Map<String, Object> location = new LinkedHashMap<>();
                location.put("id", n.id);
                location.put("name", n.name);
                location.put("lat", n.lat);
                location.put("lng", n.lng);
                return location;
            })
            .toList();
    }

    /**
     * Find shortest route between two nodes.
     * @param from   source node ID (e.g. "CP")
     * @param to     destination node ID (e.g. "IGI")
     * @param algo   "dijkstra" (default) or "astar"
     */
    @GetMapping("/route")
    public ResponseEntity<?> getRoute(
            @RequestParam String from,
            @RequestParam String to,
            @RequestParam(defaultValue = "dijkstra") String algo) {

        Graph graph = graphService.getGraph();
        if (graph.getNode(from) == null || graph.getNode(to) == null) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Invalid node ID: " + from + " or " + to));
        }

        if (algo.equalsIgnoreCase("astar")) {
            AStar.Result result = AStar.findShortestPath(graph, from, to);
            if (!result.reachable())
                return ResponseEntity.ok(Map.of("reachable", false));
            return ResponseEntity.ok(toResponse(result.path(), result.totalDistance(),
                                                result.nodesExplored(), "A*"));
        } else {
            Dijkstra.Result result = Dijkstra.findShortestPath(graph, from, to);
            if (!result.reachable())
                return ResponseEntity.ok(Map.of("reachable", false));
            return ResponseEntity.ok(toResponse(result.path(), result.totalDistance(),
                                                result.nodesExplored(), "Dijkstra"));
        }
    }

    /**
     * Find top-K shortest routes using Yen's algorithm.
     * @param k  number of alternative routes (default 3, max 10)
     */
    @GetMapping("/route/k")
    public ResponseEntity<?> getKRoutes(
            @RequestParam String from,
            @RequestParam String to,
            @RequestParam(defaultValue = "3") int k) {

        Graph graph = graphService.getGraph();
        if (graph.getNode(from) == null || graph.getNode(to) == null) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Invalid node ID: " + from + " or " + to));
        }

        if (k < 1) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "k must be at least 1"));
        }
        k = Math.min(k, 10); // safety cap
        YensKShortest.KResult result = YensKShortest.findKShortest(graph, from, to, k);

        if (result.paths().isEmpty())
            return ResponseEntity.ok(Map.of("reachable", false, "paths", List.of()));

        List<RouteResponse> responses = new ArrayList<>();
        for (int i = 0; i < result.paths().size(); i++) {
            responses.add(toResponse(result.paths().get(i), result.distances().get(i), -1,
                                     "Yen's K-Shortest (rank " + (i + 1) + ")"));
        }
        return ResponseEntity.ok(Map.of(
            "from", graph.getNode(from).name,
            "to",   graph.getNode(to).name,
            "k",    result.paths().size(),
            "routes", responses
        ));
    }

    /**
     * Benchmark Dijkstra vs A* on the same query.
     * Returns both paths, distances, nodes explored, and avg timing over 50 runs.
     */
    @GetMapping("/route/benchmark")
    public ResponseEntity<?> getBenchmark(
            @RequestParam String from,
            @RequestParam String to) {

        Graph graph = graphService.getGraph();
        if (graph.getNode(from) == null || graph.getNode(to) == null) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Invalid node ID: " + from + " or " + to));
        }

        // Warmup
        for (int i = 0; i < 5; i++) {
            Dijkstra.findShortestPath(graph, from, to);
            AStar.findShortestPath(graph, from, to);
        }

        // Timed
        int runs = 50;
        long dTotal = 0, aTotal = 0;
        Dijkstra.Result dr = null;
        AStar.Result ar = null;
        for (int i = 0; i < runs; i++) {
            long t = System.nanoTime(); dr = Dijkstra.findShortestPath(graph, from, to); dTotal += System.nanoTime() - t;
            t = System.nanoTime(); ar = AStar.findShortestPath(graph, from, to);     aTotal += System.nanoTime() - t;
        }

        int saved = dr.nodesExplored() - ar.nodesExplored();
        double pct = dr.nodesExplored() > 0 ? 100.0 * saved / dr.nodesExplored() : 0;

        return ResponseEntity.ok(Map.of(
            "from", graph.getNode(from).name,
            "to",   graph.getNode(to).name,
            "dijkstra", Map.of(
                "path", dr.path().stream().map(n -> n.name).toList(),
                "distanceKm", dr.totalDistance(),
                "nodesExplored", dr.nodesExplored(),
                "avgTimeMs", dTotal / (double) runs / 1_000_000.0
            ),
            "aStar", Map.of(
                "path", ar.path().stream().map(n -> n.name).toList(),
                "distanceKm", ar.totalDistance(),
                "nodesExplored", ar.nodesExplored(),
                "avgTimeMs", aTotal / (double) runs / 1_000_000.0
            ),
            "aStarNodeReduction", String.format("%.0f%%", pct)
        ));
    }

    /**
     * Toggle a road closure.
     * POST /api/road/close?from=CP&to=INA&closed=true
     */
    @PostMapping("/road/close")
    public ResponseEntity<?> setRoadClosure(
            @RequestParam String from,
            @RequestParam String to,
            @RequestParam boolean closed) {

        Graph graph = graphService.getGraph();
        var edgeOpt = graph.getEdges(from).stream()
            .filter(e -> e.to.id.equals(to))
            .findFirst();

        if (edgeOpt.isEmpty())
            return ResponseEntity.badRequest().body(Map.of("error", "Road not found: " + from + " → " + to));

        edgeOpt.get().setClosed(closed);
        return ResponseEntity.ok(Map.of(
            "road", from + " → " + to,
            "status", closed ? "CLOSED" : "OPEN"
        ));
    }

    private RouteResponse toResponse(List<Node> path, double dist, int explored, String algo) {
        return new RouteResponse(
            path.stream().map(RouteResponse.Stop::from).toList(),
            dist, explored, algo
        );
    }
}
