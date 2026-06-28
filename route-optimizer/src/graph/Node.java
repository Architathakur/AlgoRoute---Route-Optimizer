package graph;

/**
 * Represents a location in the city (intersection, landmark, etc.)
 * Stores coordinates for potential A* heuristic use in Tier 2.
 */
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

    @Override
    public String toString() {
        return name + " (" + id + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Node)) return false;
        return id.equals(((Node) o).id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
