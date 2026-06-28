package algorithms;

import graph.Edge;
import graph.Graph;
import graph.Node;

import java.util.*;

/**
 * Dijkstra's Shortest Path Algorithm
 *
 * How it works (interview explanation):
 *  1. Start with source node at distance 0, all others at infinity.
 *  2. Use a min-heap (priority queue) to always expand the closest unvisited node.
 *  3. For each neighbor, if going through the current node is cheaper, update its distance.
 *  4. Repeat until destination is reached (or all nodes processed).
 *
 * Time:  O((V + E) log V) with a binary heap
 * Space: O(V) for distances + predecessor maps
 *
 * Why Dijkstra over BFS? BFS finds fewest hops; Dijkstra finds minimum WEIGHT path.
 * Why not Bellman-Ford? Dijkstra is faster for non-negative weights (our case).
 */
public class Dijkstra {

    /** Internal record for the priority queue */
    private record Entry(String nodeId, double dist) implements Comparable<Entry> {
        @Override
        public int compareTo(Entry other) {
            return Double.compare(this.dist, other.dist);
        }
    }

    /** Result object returned to the caller */
    public static class Result {
        public final List<Node> path;          // ordered nodes from source to destination
        public final double totalDistance;     // total route cost
        public final int nodesExplored;        // for benchmarking vs A*
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
     * Runs Dijkstra from sourceId to destId on the given graph.
     * Respects edge.active — closed roads are skipped.
     */
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
            return new Result(Collections.emptyList(), -1, 0);
        }

        Map<String, Double> dist = new HashMap<>();
        Map<String, String> prev = new HashMap<>();  // predecessor map for path reconstruction
        Set<String> visited = new HashSet<>();
        PriorityQueue<Entry> pq = new PriorityQueue<>();
        int nodesExplored = 0;

        // Initialize all distances to infinity
        for (Node node : graph.getAllNodes()) {
            dist.put(node.id, Double.MAX_VALUE);
        }
        dist.put(sourceId, 0.0);
        pq.offer(new Entry(sourceId, 0.0));

        while (!pq.isEmpty()) {
            Entry current = pq.poll();
            String currId = current.nodeId;

            if (visited.contains(currId)) continue;  // stale entry in PQ
            visited.add(currId);
            nodesExplored++;

            // Early exit — we've found the shortest path to destination
            if (currId.equals(destId)) break;

            for (Edge edge : graph.getEdges(currId)) {
                if (!edge.active) continue;  // skip closed roads
                if (blockedEdges.contains(edgeKey(currId, edge.to.id))) continue;
                if (blockedNodes.contains(edge.to.id)) continue;

                String neighborId = edge.to.id;
                double newDist = dist.get(currId) + edge.effectiveWeight();

                if (newDist < dist.getOrDefault(neighborId, Double.MAX_VALUE)) {
                    dist.put(neighborId, newDist);
                    prev.put(neighborId, currId);
                    pq.offer(new Entry(neighborId, newDist));
                }
            }
        }

        // Reconstruct path by walking backwards through predecessor map
        List<Node> path = reconstructPath(graph, prev, sourceId, destId);
        double totalDist = dist.getOrDefault(destId, Double.MAX_VALUE);

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

        // Verify path starts at source (handles disconnected case)
        if (path.isEmpty() || !path.getFirst().id.equals(sourceId)) {
            return Collections.emptyList();
        }
        return path;
    }

    static String edgeKey(String fromId, String toId) {
        return fromId + "\u0000" + toId;
    }
}
