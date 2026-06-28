package com.routeoptimizer.graph;

import java.util.*;

public class Graph {
    private final Map<String, Node> nodes = new HashMap<>();
    private final Map<String, List<Edge>> adjList = new HashMap<>();

    public void addNode(Node node) {
        nodes.put(node.id, node);
        adjList.putIfAbsent(node.id, new ArrayList<>());
    }

    public Node getNode(String id) { return nodes.get(id); }
    public Collection<Node> getAllNodes() { return nodes.values(); }

    public Edge addEdge(String fromId, String toId, double weight) {
        Node from = nodes.get(fromId);
        Node to = nodes.get(toId);
        if (from == null || to == null)
            throw new IllegalArgumentException("Node not found: " + (from == null ? fromId : toId));
        Edge edge = new Edge(from, to, weight);
        adjList.get(fromId).add(edge);
        return edge;
    }

    public void addRoad(String id1, String id2, double weight) {
        addEdge(id1, id2, weight);
        addEdge(id2, id1, weight);
    }

    public List<Edge> getEdges(String nodeId) {
        return adjList.getOrDefault(nodeId, Collections.emptyList());
    }

    public int nodeCount() { return nodes.size(); }
    public int edgeCount() { return adjList.values().stream().mapToInt(List::size).sum(); }
}
