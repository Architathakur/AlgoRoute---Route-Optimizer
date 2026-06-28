package com.routeoptimizer.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.routeoptimizer.graph.Graph;
import com.routeoptimizer.graph.Node;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.io.InputStream;

/**
 * Spring-managed singleton that loads and exposes the city graph.
 * @PostConstruct runs once on startup — graph is loaded from classpath JSON.
 */
@Service
public class GraphService {

    private Graph graph;

    @PostConstruct
    public void init() throws Exception {
        graph = new Graph();
        ObjectMapper mapper = new ObjectMapper();

        InputStream is = getClass().getClassLoader().getResourceAsStream("delhi_map.json");
        if (is == null) throw new IllegalStateException("delhi_map.json not found in classpath");

        JsonNode root = mapper.readTree(is);

        // Load nodes
        for (JsonNode n : root.get("nodes")) {
            graph.addNode(new Node(
                n.get("id").asText(),
                n.get("name").asText(),
                n.get("lat").asDouble(),
                n.get("lng").asDouble()
            ));
        }

        // Load edges
        for (JsonNode e : root.get("edges")) {
            String from = e.get("from").asText();
            String to   = e.get("to").asText();
            double w    = e.get("weight").asDouble();
            boolean bi  = e.has("bidirectional") && e.get("bidirectional").asBoolean();
            if (bi) graph.addRoad(from, to, w);
            else    graph.addEdge(from, to, w);
        }

        System.out.printf("✓ Graph loaded: %d nodes, %d edges%n",
                          graph.nodeCount(), graph.edgeCount());
    }

    public Graph getGraph() { return graph; }
}
