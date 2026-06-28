package com.routeoptimizer.algorithms;

import com.routeoptimizer.graph.Edge;
import com.routeoptimizer.graph.Graph;
import com.routeoptimizer.graph.Node;

import java.util.*;

public class Dijkstra {

    private record Entry(String nodeId, double dist) implements Comparable<Entry> {
        public int compareTo(Entry o) { return Double.compare(dist, o.dist); }
    }

    public record Result(List<Node> path, double totalDistance, int nodesExplored, boolean reachable) {
        public Result(List<Node> path, double totalDistance, int nodesExplored) {
            this(path, totalDistance, nodesExplored, !path.isEmpty());
        }
    }

    public static Result findShortestPath(Graph graph, String sourceId, String destId) {
        return findShortestPath(graph, sourceId, destId, Set.of(), Set.of());
    }

    static Result findShortestPath(
            Graph graph,
            String sourceId,
            String destId,
            Set<String> blockedEdges,
            Set<String> blockedNodes) {
        if (graph.getNode(sourceId) == null || graph.getNode(destId) == null ||
                blockedNodes.contains(sourceId) || blockedNodes.contains(destId)) {
            return new Result(List.of(), -1, 0);
        }

        Map<String, Double> dist = new HashMap<>();
        Map<String, String> prev = new HashMap<>();
        Set<String> visited = new HashSet<>();
        PriorityQueue<Entry> pq = new PriorityQueue<>();
        int nodesExplored = 0;

        for (Node n : graph.getAllNodes()) dist.put(n.id, Double.MAX_VALUE);
        dist.put(sourceId, 0.0);
        pq.offer(new Entry(sourceId, 0.0));

        while (!pq.isEmpty()) {
            Entry cur = pq.poll();
            if (visited.contains(cur.nodeId())) continue;
            visited.add(cur.nodeId());
            nodesExplored++;
            if (cur.nodeId().equals(destId)) break;

            for (Edge e : graph.getEdges(cur.nodeId())) {
                if (!e.active) continue;
                if (blockedEdges.contains(edgeKey(cur.nodeId(), e.to.id))) continue;
                if (blockedNodes.contains(e.to.id)) continue;
                double nd = dist.get(cur.nodeId()) + e.effectiveWeight();
                if (nd < dist.getOrDefault(e.to.id, Double.MAX_VALUE)) {
                    dist.put(e.to.id, nd);
                    prev.put(e.to.id, cur.nodeId());
                    pq.offer(new Entry(e.to.id, nd));
                }
            }
        }

        double total = dist.getOrDefault(destId, Double.MAX_VALUE);
        if (total == Double.MAX_VALUE) return new Result(List.of(), -1, nodesExplored);
        return new Result(reconstruct(graph, prev, sourceId, destId), total, nodesExplored);
    }

    static List<Node> reconstruct(Graph graph, Map<String, String> prev, String src, String dest) {
        LinkedList<Node> path = new LinkedList<>();
        String cur = dest;
        while (cur != null) {
            Node n = graph.getNode(cur);
            if (n == null) return List.of();
            path.addFirst(n);
            if (cur.equals(src)) break;
            cur = prev.get(cur);
        }
        if (path.isEmpty() || !path.getFirst().id.equals(src)) return List.of();
        return path;
    }

    static String edgeKey(String fromId, String toId) {
        return fromId + "\u0000" + toId;
    }
}
