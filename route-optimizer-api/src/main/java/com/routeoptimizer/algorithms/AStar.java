package com.routeoptimizer.algorithms;

import com.routeoptimizer.graph.Edge;
import com.routeoptimizer.graph.Graph;
import com.routeoptimizer.graph.Node;

import java.util.*;

public class AStar {

    private record Entry(String nodeId, double fScore) implements Comparable<Entry> {
        public int compareTo(Entry o) { return Double.compare(fScore, o.fScore); }
    }

    public record Result(List<Node> path, double totalDistance, int nodesExplored, boolean reachable) {
        public Result(List<Node> path, double totalDistance, int nodesExplored) {
            this(path, totalDistance, nodesExplored, !path.isEmpty());
        }
    }

    public static double haversine(Node a, Node b) {
        final double R = 6371.0;
        double dLat = Math.toRadians(b.lat - a.lat);
        double dLng = Math.toRadians(b.lng - a.lng);
        double h = Math.pow(Math.sin(dLat / 2), 2)
                 + Math.cos(Math.toRadians(a.lat)) * Math.cos(Math.toRadians(b.lat))
                   * Math.pow(Math.sin(dLng / 2), 2);
        return 2 * R * Math.asin(Math.sqrt(h));
    }

    public static Result findShortestPath(Graph graph, String sourceId, String destId) {
        Node source = graph.getNode(sourceId);
        Node dest = graph.getNode(destId);
        if (source == null || dest == null) return new Result(List.of(), -1, 0);

        Map<String, Double> gScore = new HashMap<>();
        Map<String, String> prev = new HashMap<>();
        Set<String> visited = new HashSet<>();
        PriorityQueue<Entry> pq = new PriorityQueue<>();
        int nodesExplored = 0;

        for (Node n : graph.getAllNodes()) gScore.put(n.id, Double.MAX_VALUE);
        gScore.put(sourceId, 0.0);
        pq.offer(new Entry(sourceId, haversine(source, dest)));

        while (!pq.isEmpty()) {
            Entry cur = pq.poll();
            if (visited.contains(cur.nodeId())) continue;
            visited.add(cur.nodeId());
            nodesExplored++;
            if (cur.nodeId().equals(destId)) break;

            for (Edge e : graph.getEdges(cur.nodeId())) {
                if (!e.active) continue;
                double ng = gScore.get(cur.nodeId()) + e.effectiveWeight();
                if (ng < gScore.getOrDefault(e.to.id, Double.MAX_VALUE)) {
                    gScore.put(e.to.id, ng);
                    prev.put(e.to.id, cur.nodeId());
                    pq.offer(new Entry(e.to.id, ng + haversine(e.to, dest)));
                }
            }
        }

        double total = gScore.getOrDefault(destId, Double.MAX_VALUE);
        if (total == Double.MAX_VALUE) return new Result(List.of(), -1, nodesExplored);
        return new Result(Dijkstra.reconstruct(graph, prev, sourceId, destId), total, nodesExplored);
    }
}
