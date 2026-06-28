package com.routeoptimizer.graph;

public class Edge {
    public final Node from;
    public final Node to;
    public double weight;
    public boolean active;
    public double trafficMultiplier;

    public Edge(Node from, Node to, double weight) {
        this.from = from;
        this.to = to;
        this.weight = weight;
        this.active = true;
        this.trafficMultiplier = 1.0;
    }

    public double effectiveWeight() { return weight * trafficMultiplier; }
    public void setClosed(boolean closed) { this.active = !closed; }
}
