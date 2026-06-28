package algorithms;

import graph.Edge;
import graph.Graph;
import graph.Node;

import java.util.*;

/**
 * A* Shortest Path Algorithm
 *
 * How it works (interview explanation):
 *  A* improves on Dijkstra by using a heuristic h(n) to ESTIMATE the cost
 *  from node n to the destination. Instead of just prioritizing by cost-so-far
 *  g(n), it prioritizes by f(n) = g(n) + h(n).
 *
 *  This means the search is guided TOWARD the destination instead of expanding
 *  outward in all directions like Dijkstra does.
 *
 * Heuristic used: Haversine distance (straight-line distance between lat/lng coords)
 *  - Always admissible: straight-line ≤ actual road distance, so A* never overestimates
 *  - Admissibility guarantees the path found is still OPTIMAL
 *
 * Time:  O((V + E) log V) — same Big-O as Dijkstra, but fewer nodes expanded in practice
 * Space: O(V)
 *
 * When does A* beat Dijkstra?
 *  - City grids, maps — good spatial heuristic available
 *  - Large graphs where you only need ONE destination (not all-pairs)
 *
 * When is Dijkstra better?
 *  - No coordinates available (heuristic = 0, A* degrades to Dijkstra anyway)
 *  - Need shortest path to ALL nodes (single-source, multi-destination)
 */
public class AStar {

    private record Entry(String nodeId, double fScore) implements Comparable<Entry> {
        @Override
        public int compareTo(Entry other) {
            return Double.compare(this.fScore, other.fScore);
        }
    }

    public static class Result {
        public final List<Node> path;
        public final double totalDistance;
        public final int nodesExplored;
        public final boolean reachable;

        public Result(List<Node> path, double totalDistance, int nodesExplored) {
            this.path = path;
            this.totalDistance = totalDistance;
            this.nodesExplored = nodesExplored;
            this.reachable = !path.isEmpty();
        }

        public void print() {
            if (!reachable) {
                System.out.println("  ✗ No path found (destination unreachable).");
                return;
            }
            System.out.println("\n  Route:");
            for (int i = 0; i < path.size(); i++) {
                String prefix = (i == 0) ? "  START " : (i == path.size() - 1) ? "  END   " : "        ";
                System.out.println(prefix + "→ " + path.get(i).name);
            }
            System.out.printf("%n  Total distance : %.2f km%n", totalDistance);
            System.out.printf("  Nodes explored : %d%n%n", nodesExplored);
        }
    }

    /**
     * Haversine formula — great-circle distance between two lat/lng points.
     * Returns distance in km. Used as the A* heuristic h(n).
     *
     * Why haversine? The Earth is a sphere, so straight-line Euclidean distance
     * between degrees of latitude/longitude is inaccurate at scale.
     * Haversine accounts for Earth's curvature.
     */
    public static double haversine(Node a, Node b) {
        final double R = 6371.0; // Earth's radius in km
        double dLat = Math.toRadians(b.lat - a.lat);
        double dLng = Math.toRadians(b.lng - a.lng);
        double sinLat = Math.sin(dLat / 2);
        double sinLng = Math.sin(dLng / 2);
        double h = sinLat * sinLat +
                   Math.cos(Math.toRadians(a.lat)) * Math.cos(Math.toRadians(b.lat)) * sinLng * sinLng;
        return 2 * R * Math.asin(Math.sqrt(h));
    }

    /**
     * Runs A* from sourceId to destId.
     * Respects edge.active — closed roads are skipped.
     */
    public static Result findShortestPath(Graph graph, String sourceId, String destId) {
        Node source = graph.getNode(sourceId);
        Node dest = graph.getNode(destId);
        if (source == null || dest == null) return new Result(Collections.emptyList(), -1, 0);

        Map<String, Double> gScore = new HashMap<>();   // cost from source to node
        Map<String, Double> fScore = new HashMap<>();   // gScore + heuristic
        Map<String, String> prev = new HashMap<>();
        Set<String> visited = new HashSet<>();
        PriorityQueue<Entry> pq = new PriorityQueue<>();
        int nodesExplored = 0;

        for (Node node : graph.getAllNodes()) {
            gScore.put(node.id, Double.MAX_VALUE);
            fScore.put(node.id, Double.MAX_VALUE);
        }

        gScore.put(sourceId, 0.0);
        fScore.put(sourceId, haversine(source, dest));
        pq.offer(new Entry(sourceId, fScore.get(sourceId)));

        while (!pq.isEmpty()) {
            Entry current = pq.poll();
            String currId = current.nodeId;

            if (visited.contains(currId)) continue;
            visited.add(currId);
            nodesExplored++;

            if (currId.equals(destId)) break;

            for (Edge edge : graph.getEdges(currId)) {
                if (!edge.active) continue;

                String neighborId = edge.to.id;
                double tentativeG = gScore.get(currId) + edge.effectiveWeight();

                if (tentativeG < gScore.getOrDefault(neighborId, Double.MAX_VALUE)) {
                    prev.put(neighborId, currId);
                    gScore.put(neighborId, tentativeG);
                    double h = haversine(edge.to, dest);
                    double f = tentativeG + h;
                    fScore.put(neighborId, f);
                    pq.offer(new Entry(neighborId, f));
                }
            }
        }

        List<Node> path = reconstructPath(graph, prev, sourceId, destId);
        double totalDist = gScore.getOrDefault(destId, Double.MAX_VALUE);

        if (totalDist == Double.MAX_VALUE) {
            return new Result(Collections.emptyList(), -1, nodesExplored);
        }
        return new Result(path, totalDist, nodesExplored);
    }

    private static List<Node> reconstructPath(Graph graph, Map<String, String> prev,
                                               String sourceId, String destId) {
        LinkedList<Node> path = new LinkedList<>();
        String current = destId;
        while (current != null) {
            Node node = graph.getNode(current);
            if (node == null) return Collections.emptyList();
            path.addFirst(node);
            if (current.equals(sourceId)) break;
            current = prev.get(current);
        }
        if (path.isEmpty() || !path.getFirst().id.equals(sourceId)) {
            return Collections.emptyList();
        }
        return path;
    }
}
