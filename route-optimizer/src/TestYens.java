import algorithms.Dijkstra;
import algorithms.YensKShortest;
import graph.Graph;
import graph.Node;
import io.MapLoader;

import java.util.List;

/** Quick standalone test for Yen's K-Shortest algorithm */
public class TestYens {
    public static void main(String[] args) throws Exception {
        Graph graph = MapLoader.loadFromFile("data/delhi_map.json");

        String[][] queries = {
            {"CP", "IGI", "Connaught Place → IGI Airport"},
            {"DWK", "NFC", "Dwarka → Nehru Place"},
            {"KSY", "SKV", "Kashmere Gate → Saket"}
        };

        for (String[] q : queries) {
            System.out.println("\n══════════════════════════════════════════════════");
            System.out.println("  Top-3 Routes: " + q[2]);
            System.out.println("══════════════════════════════════════════════════");

            YensKShortest.KResult result = YensKShortest.findKShortest(graph, q[0], q[1], 3);

            if (result.paths().isEmpty()) {
                System.out.println("  No paths found.");
                continue;
            }

            for (int i = 0; i < result.paths().size(); i++) {
                List<Node> path = result.paths().get(i);
                double dist = result.distances().get(i);

                System.out.printf("%n  Route #%d  (%.2f km):%n", i + 1, dist);
                for (int j = 0; j < path.size(); j++) {
                    String prefix = j == 0 ? "  START" : j == path.size()-1 ? "  END  " : "      ";
                    System.out.println(prefix + " → " + path.get(j).name);
                }
            }

            // Verify: route #1 must match Dijkstra
            Dijkstra.Result dijk = Dijkstra.findShortestPath(graph, q[0], q[1]);
            boolean match = Math.abs(result.distances().get(0) - dijk.totalDistance) < 0.001;
            System.out.printf("%n  ✓ Route #1 matches Dijkstra: %s (%.2f km vs %.2f km)%n",
                match ? "YES" : "NO", result.distances().get(0), dijk.totalDistance);
        }
    }
}
