package io;

import graph.Edge;
import graph.Graph;
import graph.Node;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;

/**
 * Loads a city map from a simple JSON file into a Graph.
 *
 * Format expected:
 * {
 *   "nodes": [
 *     { "id": "A", "name": "Connaught Place", "lat": 28.6315, "lng": 77.2167 },
 *     ...
 *   ],
 *   "edges": [
 *     { "from": "A", "to": "B", "weight": 3.2, "bidirectional": true },
 *     ...
 *   ]
 * }
 *
 * Note: Uses lightweight regex parsing to avoid external JSON library dependencies.
 * In a production system, use Gson or Jackson instead.
 */
public class MapLoader {

    public static Graph loadFromFile(String filePath) throws IOException {
        String content = Files.readString(Path.of(filePath));
        Graph graph = new Graph();

        // Parse nodes
        List<Map<String, String>> nodes = parseObjectArray(content, "nodes");
        for (Map<String, String> node : nodes) {
            graph.addNode(new Node(
                node.get("id"),
                node.get("name"),
                Double.parseDouble(node.getOrDefault("lat", "0")),
                Double.parseDouble(node.getOrDefault("lng", "0"))
            ));
        }

        // Parse edges
        List<Map<String, String>> edges = parseObjectArray(content, "edges");
        for (Map<String, String> edge : edges) {
            String from = edge.get("from");
            String to = edge.get("to");
            double weight = Double.parseDouble(edge.get("weight"));
            boolean bidirectional = "true".equalsIgnoreCase(edge.getOrDefault("bidirectional", "false"));

            if (bidirectional) {
                graph.addRoad(from, to, weight);
            } else {
                graph.addEdge(from, to, weight);
            }
        }

        System.out.println("✓ Loaded map: " + graph.nodeCount() + " locations, " +
                           graph.edgeCount() + " roads from " + filePath);
        return graph;
    }

    /** Minimal JSON array parser — extracts key-value pairs from objects in an array */
    private static List<Map<String, String>> parseObjectArray(String json, String key) {
        List<Map<String, String>> result = new ArrayList<>();

        // Extract the array block for the given key
        Pattern arrayPattern = Pattern.compile("\"" + key + "\"\\s*:\\s*\\[(.*?)]", Pattern.DOTALL);
        Matcher arrayMatcher = arrayPattern.matcher(json);
        if (!arrayMatcher.find()) return result;

        String arrayContent = arrayMatcher.group(1);

        // Extract individual objects { ... }
        Pattern objPattern = Pattern.compile("\\{([^}]+)}", Pattern.DOTALL);
        Matcher objMatcher = objPattern.matcher(arrayContent);

        while (objMatcher.find()) {
            String objContent = objMatcher.group(1);
            Map<String, String> map = new LinkedHashMap<>();

            // Extract "key": "value" or "key": number/boolean pairs
            Pattern kvPattern = Pattern.compile("\"(\\w+)\"\\s*:\\s*\"?([^,\"\\}]+)\"?");
            Matcher kvMatcher = kvPattern.matcher(objContent);
            while (kvMatcher.find()) {
                map.put(kvMatcher.group(1).trim(), kvMatcher.group(2).trim());
            }
            if (!map.isEmpty()) result.add(map);
        }

        return result;
    }
}
