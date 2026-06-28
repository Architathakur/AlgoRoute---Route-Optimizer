package com.routeoptimizer.graph;

public class Node {
    public final String id;
    public final String name;
    public final double lat;
    public final double lng;

    public Node(String id, String name, double lat, double lng) {
        this.id = id;
        this.name = name;
        this.lat = lat;
        this.lng = lng;
    }

    @Override public String toString() { return name + " (" + id + ")"; }
    @Override public boolean equals(Object o) {
        if (!(o instanceof Node n)) return false;
        return id.equals(n.id);
    }
    @Override public int hashCode() { return id.hashCode(); }
}
