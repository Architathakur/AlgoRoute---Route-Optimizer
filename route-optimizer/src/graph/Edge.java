package graph;

/**
 * Represents a road segment between two city locations.
 * - weight: base travel cost (distance in km or time in minutes)
 * - active: can be toggled to simulate road closures (Tier 2)
 * - trafficMultiplier: scales weight to simulate traffic (Tier 2)
 */
public class Edge {
    public final Node from;
    public final Node to;
    public double weight;
    public boolean active;
    public double trafficMultiplier; // 1.0 = no traffic

    public Edge(Node from, Node to, double weight) {
        this.from = from;
        this.to = to;
        this.weight = weight;
        this.active = true;
        this.trafficMultiplier = 1.0;
    }

    /** Effective cost accounting for traffic */
    public double effectiveWeight() {
        return weight * trafficMultiplier;
    }

    /** Toggle road closure on/off */
    public void setClosed(boolean closed) {
        this.active = !closed;
    }

    @Override
    public String toString() {
        String status = active ? "" : " [CLOSED]";
        return from.name + " → " + to.name +
               " (" + String.format("%.1f", effectiveWeight()) + " km)" + status;
    }
}
