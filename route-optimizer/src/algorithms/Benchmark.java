package algorithms;

import graph.Graph;
import graph.Node;

import java.util.List;

/**
 * Benchmarks Dijkstra vs A* on the same query.
 *
 * Metrics compared:
 *  - Nodes explored  (efficiency — fewer = better guided search)
 *  - Total distance  (correctness — must be identical for both)
 *  - Wall-clock time (speed — averaged over multiple runs for accuracy)
 */
public class Benchmark {

    private static final int WARMUP_RUNS  = 5;
    private static final int TIMED_RUNS   = 50;

    public record BenchmarkResult(
        String from, String to,
        double distanceDijkstra, double distanceAStar,
        int nodesDijkstra,       int nodesAStar,
        double timeDijkstraMs,   double timeAStarMs,
        List<Node> pathDijkstra, List<Node> pathAStar
    ) {}

    public static BenchmarkResult run(Graph graph, String fromId, String toId) {
        // Warmup — avoids JIT cold-start skewing results
        for (int i = 0; i < WARMUP_RUNS; i++) {
            Dijkstra.findShortestPath(graph, fromId, toId);
            AStar.findShortestPath(graph, fromId, toId);
        }

        // Timed runs — Dijkstra
        long dijkstraTotal = 0;
        Dijkstra.Result dijkstraResult = null;
        for (int i = 0; i < TIMED_RUNS; i++) {
            long t = System.nanoTime();
            dijkstraResult = Dijkstra.findShortestPath(graph, fromId, toId);
            dijkstraTotal += System.nanoTime() - t;
        }

        // Timed runs — A*
        long astarTotal = 0;
        AStar.Result astarResult = null;
        for (int i = 0; i < TIMED_RUNS; i++) {
            long t = System.nanoTime();
            astarResult = AStar.findShortestPath(graph, fromId, toId);
            astarTotal += System.nanoTime() - t;
        }

        return new BenchmarkResult(
            graph.getNode(fromId).name,
            graph.getNode(toId).name,
            dijkstraResult.totalDistance, astarResult.totalDistance,
            dijkstraResult.nodesExplored, astarResult.nodesExplored,
            dijkstraTotal / (double) TIMED_RUNS / 1_000_000.0,
            astarTotal    / (double) TIMED_RUNS / 1_000_000.0,
            dijkstraResult.path, astarResult.path
        );
    }

    public static void print(BenchmarkResult r) {
        boolean sameDistance = Math.abs(r.distanceDijkstra() - r.distanceAStar()) < 0.001;
        int nodesSaved = r.nodesDijkstra() - r.nodesAStar();
        double nodePct = r.nodesDijkstra() > 0
            ? (100.0 * nodesSaved / r.nodesDijkstra()) : 0;
        double speedup = r.timeDijkstraMs() > 0
            ? r.timeDijkstraMs() / r.timeAStarMs() : 1.0;

        System.out.println("\n┌─────────────────────────────────────────────────────┐");
        System.out.printf( "│  %s  →  %s%n", r.from(), r.to());
        System.out.println("├──────────────────┬──────────────┬──────────────────-─┤");
        System.out.println("│ Metric           │  Dijkstra    │  A*                │");
        System.out.println("├──────────────────┼──────────────┼────────────────────┤");
        System.out.printf( "│ Distance (km)    │  %8.2f    │  %8.2f  %s        │%n",
            r.distanceDijkstra(), r.distanceAStar(), sameDistance ? "✓" : "✗");
        System.out.printf( "│ Nodes explored   │  %8d    │  %8d  (-%d%%)%s  │%n",
            r.nodesDijkstra(), r.nodesAStar(), (int) nodePct,
            nodesSaved >= 0 ? " ✓" : " ✗");
        System.out.printf( "│ Avg time (ms)    │  %8.4f    │  %8.4f  (%.1fx)  │%n",
            r.timeDijkstraMs(), r.timeAStarMs(), speedup);
        System.out.println("└──────────────────┴──────────────┴────────────────────┘");

        if (!sameDistance) {
            System.out.println("  ⚠ WARNING: Path distances differ — heuristic may not be admissible!");
        }
        System.out.printf("  A* skipped %d nodes (%.0f%% reduction in search space)%n%n",
            nodesSaved, nodePct);
    }
}
