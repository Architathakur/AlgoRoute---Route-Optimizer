package graph;

import java.util.*;

/**
 * Directed weighted graph representing the city road network.
 * Internally uses an adjacency list: Map<nodeId, List<Edge>>
 *
 * Design choice: adjacency list over matrix because city graphs are
 * sparse — most intersections connect to only 3-5 roads, not all nodes.
 * Space: O(V + E) vs O(V^2) for matrix.
 */
public class Graph {
    private final Map<String, Node> nodes;            // id → Node
    private final Map<String, List<Edge>> adjList;    // nodeId → outgoing edges

    public Graph() {
        this.nodes = new HashMap<>();
        this.adjList = new HashMap<>();
    }

    // ── Node operations ──────────────────────────────────────────────

    public void addNode(Node node) {
        nodes.put(node.id, node);
        adjList.putIfAbsent(node.id, new ArrayList<>());
    }

    public Node getNode(String id) {
        return nodes.get(id);
    }

    public Collection<Node> getAllNodes() {
        return nodes.values();
    }

    // ── Edge operations ──────────────────────────────────────────────

    /**
     * Adds a directed edge from → to with given weight.
     * For undirected roads, call addEdge twice (both directions).
     */
    public Edge addEdge(String fromId, String toId, double weight) {
        Node from = nodes.get(fromId);
        Node to = nodes.get(toId);
        if (from == null || to == null) {
            throw new IllegalArgumentException(
                "Node not found: " + (from == null ? fromId : toId));
        }
        Edge edge = new Edge(from, to, weight);
        adjList.get(fromId).add(edge);
        return edge;
    }

    /** Adds a bidirectional road (undirected edge) */
    public void addRoad(String id1, String id2, double weight) {
        addEdge(id1, id2, weight);
        addEdge(id2, id1, weight);
    }

    public List<Edge> getEdges(String nodeId) {
        return adjList.getOrDefault(nodeId, Collections.emptyList());
    }

    public int nodeCount() { return nodes.size(); }
    public int edgeCount() {
        return adjList.values().stream().mapToInt(List::size).sum();
    }

    /** Print the graph for debugging */
    public void printGraph() {
        System.out.println("\n=== City Graph (" + nodeCount() + " nodes, " +
                           edgeCount() + " edges) ===");
        for (String id : adjList.keySet()) {
            Node node = nodes.get(id);
            System.out.print("  " + node.name + " → ");
            List<Edge> edges = adjList.get(id);
            if (edges.isEmpty()) {
                System.out.println("(no outgoing roads)");
            } else {
                StringJoiner sj = new StringJoiner(", ");
                for (Edge e : edges) {
                    if (e.active)
                        sj.add(e.to.name + " [" + String.format("%.1f", e.effectiveWeight()) + "km]");
                    else
                        sj.add(e.to.name + " [CLOSED]");
                }
                System.out.println(sj);
            }
        }
        System.out.println();
    }
}
