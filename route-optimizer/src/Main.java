import algorithms.AStar;
import algorithms.Benchmark;
import algorithms.Dijkstra;
import graph.Graph;
import graph.Node;
import io.MapLoader;

import java.util.*;

/**
 * Smart City Route Optimizer — CLI Entry Point
 *
 * Usage:
 *   javac -d out src/graph/*.java src/algorithms/*.java src/io/*.java src/Main.java
 *   java -cp out Main
 */
public class Main {

    public static void main(String[] args) throws Exception {
        System.out.println("╔═══════════════════════════════════════╗");
        System.out.println("║   Smart City Route Optimizer v2       ║");
        System.out.println("║   Delhi Road Network                  ║");
        System.out.println("║   Dijkstra's + A* with Haversine      ║");
        System.out.println("╚═══════════════════════════════════════╝\n");

        Graph graph = MapLoader.loadFromFile("data/delhi_map.json");
        graph.printGraph();

        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        while (running) {
            printMenu();
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> findRoute(graph, scanner, "dijkstra");
                case "2" -> findRoute(graph, scanner, "astar");
                case "3" -> runBenchmark(graph, scanner);
                case "4" -> listLocations(graph);
                case "5" -> toggleRoadClosure(graph, scanner);
                case "6" -> runDemo(graph);
                case "0" -> {
                    System.out.println("\nGoodbye!");
                    running = false;
                }
                default -> System.out.println("  Invalid option. Try again.\n");
            }
        }
        scanner.close();
    }

    static void printMenu() {
        System.out.println("┌──────────────────────────────────────┐");
        System.out.println("│  1. Find route (Dijkstra's)           │");
        System.out.println("│  2. Find route (A*)                   │");
        System.out.println("│  3. Benchmark Dijkstra vs A*          │");
        System.out.println("│  4. List all locations                │");
        System.out.println("│  5. Toggle road closure               │");
        System.out.println("│  6. Run full demo                     │");
        System.out.println("│  0. Exit                              │");
        System.out.println("└──────────────────────────────────────┘");
        System.out.print("Choice: ");
    }

    static void findRoute(Graph graph, Scanner scanner, String algo) {
        listLocations(graph);
        System.out.print("Enter source location ID: ");
        String from = scanner.nextLine().trim().toUpperCase();
        System.out.print("Enter destination ID:     ");
        String to = scanner.nextLine().trim().toUpperCase();

        if (graph.getNode(from) == null || graph.getNode(to) == null) {
            System.out.println("  ✗ Invalid location ID.\n");
            return;
        }

        if (algo.equals("dijkstra")) {
            System.out.println("\nRunning Dijkstra's algorithm...");
            long start = System.nanoTime();
            Dijkstra.Result result = Dijkstra.findShortestPath(graph, from, to);
            long elapsed = System.nanoTime() - start;
            result.print();
            System.out.printf("  Computed in: %.3f ms%n%n", elapsed / 1_000_000.0);
        } else {
            System.out.println("\nRunning A* algorithm...");
            long start = System.nanoTime();
            AStar.Result result = AStar.findShortestPath(graph, from, to);
            long elapsed = System.nanoTime() - start;
            result.print();
            System.out.printf("  Computed in: %.3f ms%n%n", elapsed / 1_000_000.0);
        }
    }

    static void runBenchmark(Graph graph, Scanner scanner) {
        listLocations(graph);
        System.out.print("Enter source location ID: ");
        String from = scanner.nextLine().trim().toUpperCase();
        System.out.print("Enter destination ID:     ");
        String to = scanner.nextLine().trim().toUpperCase();

        if (graph.getNode(from) == null || graph.getNode(to) == null) {
            System.out.println("  ✗ Invalid location ID.\n");
            return;
        }

        System.out.println("\nBenchmarking (50 timed runs each, JIT warmed up)...");
        Benchmark.BenchmarkResult result = Benchmark.run(graph, from, to);
        Benchmark.print(result);
    }

    static void listLocations(Graph graph) {
        System.out.println("\n  Available locations:");
        graph.getAllNodes().stream()
            .sorted(Comparator.comparing(n -> n.id))
            .forEach(n -> System.out.printf("    %-6s %s%n", "[" + n.id + "]", n.name));
        System.out.println();
    }

    static void toggleRoadClosure(Graph graph, Scanner scanner) {
        System.out.print("Enter road FROM node ID: ");
        String from = scanner.nextLine().trim().toUpperCase();
        System.out.print("Enter road TO node ID:   ");
        String to = scanner.nextLine().trim().toUpperCase();
        System.out.print("Close road? (yes/no):    ");
        boolean close = scanner.nextLine().trim().equalsIgnoreCase("yes");

        graph.getEdges(from).stream()
            .filter(e -> e.to.id.equals(to))
            .findFirst()
            .ifPresentOrElse(
                e -> {
                    e.setClosed(close);
                    System.out.println("  ✓ Road " + from + " → " + to +
                                       (close ? " CLOSED." : " REOPENED.") + "\n");
                },
                () -> System.out.println("  ✗ Road not found.\n")
            );
    }

    static void runDemo(Graph graph) {
        System.out.println("\n══════════════════════════════════════════════");
        System.out.println("  DEMO PART 1: Side-by-side route comparison");
        System.out.println("══════════════════════════════════════════════\n");

        String[][] queries = {
            {"CP",  "IGI", "Connaught Place → IGI Airport"},
            {"KSY", "SKV", "Kashmere Gate → Saket"},
            {"DWK", "NFC", "Dwarka → Nehru Place"}
        };

        for (String[] q : queries) {
            System.out.println("▶ " + q[2]);
            System.out.println("  [Dijkstra]");
            Dijkstra.findShortestPath(graph, q[0], q[1]).print();
            System.out.println("  [A*]");
            AStar.findShortestPath(graph, q[0], q[1]).print();
            System.out.println("  ─────────────────────────────────────────");
        }

        System.out.println("\n══════════════════════════════════════════════");
        System.out.println("  DEMO PART 2: Benchmark (Dijkstra vs A*)");
        System.out.println("══════════════════════════════════════════════");

        String[][] benchQueries = {{"CP", "IGI"}, {"DWK", "NFC"}, {"KSY", "HKV"}};
        for (String[] q : benchQueries) {
            Benchmark.BenchmarkResult r = Benchmark.run(graph, q[0], q[1]);
            Benchmark.print(r);
        }

        System.out.println("\n══════════════════════════════════════════════");
        System.out.println("  DEMO PART 3: Road closure + auto-rerouting");
        System.out.println("══════════════════════════════════════════════\n");

        System.out.println("▶ LJN → HKV (normal — via INA Market)");
        Dijkstra.findShortestPath(graph, "LJN", "HKV").print();

        System.out.println("  [Closing LJN → INA road...]");
        graph.getEdges("LJN").forEach(e -> { if (e.to.id.equals("INA")) e.setClosed(true); });

        System.out.println("▶ LJN → HKV (rerouted — road closed)");
        Dijkstra.findShortestPath(graph, "LJN", "HKV").print();

        graph.getEdges("LJN").forEach(e -> { if (e.to.id.equals("INA")) e.setClosed(false); });
        System.out.println("  [Road reopened]\n");
    }
}
