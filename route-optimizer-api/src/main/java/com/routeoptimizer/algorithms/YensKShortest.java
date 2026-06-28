package com.routeoptimizer.algorithms;

import com.routeoptimizer.graph.Edge;
import com.routeoptimizer.graph.Graph;
import com.routeoptimizer.graph.Node;

import java.util.*;

/**
 * Yen's K-Shortest Loopless Paths Algorithm (1971)
 *
 * How it works (interview explanation):
 *  Yen's builds on Dijkstra to find not just the shortest path, but the K
 *  shortest LOOPLESS paths from source to destination — each one a valid
 *  alternative route.
 *
 *  The algorithm works in two phases per iteration:
 *   1. SPUR: For each node along the previous best path (the "spur node"),
 *      temporarily remove already-used edges and find a new sub-path from
 *      that node to the destination (the "spur path").
 *   2. COMBINE: Concatenate the "root path" (source → spur node from the
 *      previous best path) with the spur path → candidate path.
 *   3. Keep the K best unique candidates found so far.
 *
 * Why is this useful?
 *  In real navigation, you don't just want THE shortest route — you want
 *  alternatives in case of accidents, road closures, or driver preference.
 *  Google Maps "3 route options" is conceptually Yen's K=3.
 *
 * Time:  O(K * V * (E + V log V))
 * Space: O(K * V)
 */
public class YensKShortest {

    public record KResult(List<List<Node>> paths, List<Double> distances) {}

    public static KResult findKShortest(Graph graph, String sourceId, String destId, int k) {
        if (k <= 0) return new KResult(List.of(), List.of());

        // A: confirmed K shortest paths
        List<List<Node>> A = new ArrayList<>();
        List<Double> ADist = new ArrayList<>();

        // B: candidates (min-heap by distance)
        PriorityQueue<double[]> B = new PriorityQueue<>(Comparator.comparingDouble(x -> x[0]));
        // We store candidate info as (distance, index into candidatePaths)
        List<List<Node>> candidatePaths = new ArrayList<>();

        // Step 0: find the first shortest path
        Dijkstra.Result first = Dijkstra.findShortestPath(graph, sourceId, destId);
        if (!first.reachable()) return new KResult(List.of(), List.of());

        A.add(first.path());
        ADist.add(first.totalDistance());

        for (int kk = 1; kk < k; kk++) {
            List<Node> prevPath = A.get(kk - 1);

            // Iterate over spur nodes in the previous best path
            for (int i = 0; i < prevPath.size() - 1; i++) {
                Node spurNode = prevPath.get(i);
                List<Node> rootPath = prevPath.subList(0, i + 1);

                Set<String> blockedEdges = new HashSet<>();
                Set<String> blockedNodes = new HashSet<>();

                // Remove edges used by already-found paths that share this root
                for (List<Node> aPath : A) {
                    if (aPath.size() > i && aPath.subList(0, i + 1).equals(rootPath)) {
                        // Block the (i → i+1) edge of that path
                        Node nextInA = aPath.get(i + 1);
                        blockedEdges.add(Dijkstra.edgeKey(spurNode.id, nextInA.id));
                    }
                }

                // Keep spur paths loopless by preventing them from revisiting the root path.
                for (int ri = 0; ri < rootPath.size() - 1; ri++) {
                    blockedNodes.add(rootPath.get(ri).id);
                }

                // Find spur path from spurNode to dest
                Dijkstra.Result spurResult =
                    Dijkstra.findShortestPath(graph, spurNode.id, destId, blockedEdges, blockedNodes);

                if (!spurResult.reachable()) continue;

                // Combine root path + spur path (skip duplicate spurNode)
                List<Node> candidate = new ArrayList<>(rootPath);
                candidate.addAll(spurResult.path().subList(1, spurResult.path().size()));

                // Compute root path cost
                double rootCost = rootPathCost(graph, rootPath);
                double totalCost = rootCost + spurResult.totalDistance();

                // Check if candidate already exists in B
                boolean duplicate = false;
                for (List<Node> aPath : A) {
                    if (aPath.equals(candidate)) { duplicate = true; break; }
                }
                for (List<Node> cp : candidatePaths) {
                    if (cp.equals(candidate)) { duplicate = true; break; }
                }

                if (!duplicate) {
                    int idx = candidatePaths.size();
                    candidatePaths.add(candidate);
                    B.offer(new double[]{totalCost, idx});
                }
            }

            if (B.isEmpty()) break;

            // Move best candidate from B to A
            double[] best = B.poll();
            A.add(candidatePaths.get((int) best[1]));
            ADist.add(best[0]);
        }

        return new KResult(Collections.unmodifiableList(A), Collections.unmodifiableList(ADist));
    }

    private static double rootPathCost(Graph graph, List<Node> path) {
        double cost = 0;
        for (int i = 0; i < path.size() - 1; i++) {
            String from = path.get(i).id;
            String to = path.get(i + 1).id;
            cost += graph.getEdges(from).stream()
                .filter(e -> e.to.id.equals(to))
                .mapToDouble(Edge::effectiveWeight)
                .min()
                .orElse(0);
        }
        return cost;
    }
}
