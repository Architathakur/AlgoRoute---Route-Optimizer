# Smart City Route Optimizer

A Java-based route optimization project for modeling a city road network as a weighted directed graph. The project includes both a standalone command-line application and a Spring Boot REST API for finding optimal and alternative routes across a sample Delhi road network.

The implementation supports Dijkstra's algorithm, A* search with a haversine heuristic, Yen's K-shortest loopless paths, road closure toggling, and route benchmarking.

## Features

| Capability | Status |
| --- | --- |
| Weighted directed graph using an adjacency list | Implemented |
| Load road network data from JSON | Implemented |
| Shortest path with Dijkstra's algorithm | Implemented |
| Shortest path with A* search and haversine heuristic | Implemented |
| Dijkstra vs A* benchmarking | Implemented |
| Top-K alternative routes with Yen's algorithm | Implemented |
| Road closure toggling | Implemented |
| Traffic multiplier support on edges | Implemented in graph model |
| Spring Boot REST API | Implemented |
| Standalone interactive CLI | Implemented |

## Project Structure

```text
route-optimizer/
+-- route-optimizer/
|   +-- README.md
|   +-- data/
|   |   +-- delhi_map.json
|   +-- src/
|       +-- Main.java
|       +-- TestYens.java
|       +-- algorithms/
|       |   +-- AStar.java
|       |   +-- Benchmark.java
|       |   +-- Dijkstra.java
|       |   +-- YensKShortest.java
|       +-- graph/
|       |   +-- Edge.java
|       |   +-- Graph.java
|       |   +-- Node.java
|       +-- io/
|           +-- MapLoader.java
+-- route-optimizer-api/
    +-- pom.xml
    +-- src/main/
        +-- java/com/routeoptimizer/
        |   +-- RouteOptimizerApplication.java
        |   +-- algorithms/
        |   +-- api/
        |   +-- graph/
        +-- resources/
            +-- application.properties
            +-- delhi_map.json
```

## Requirements

- Java 21 or later for the Spring Boot API
- Maven 3.9 or later for the Spring Boot API
- Java 17 or later for the standalone CLI

## Running the Spring Boot API

From the API module:

```bash
cd /Users/archita/Documents/Projects/route-optimizer/route-optimizer-api
mvn spring-boot:run
```

The API starts on:

```text
http://localhost:8080
```

Build and test the API:

```bash
mvn test
mvn package
```

Run the packaged application:

```bash
java -jar target/route-optimizer-api-1.0.0.jar
```

## API Endpoints

### List Locations

```bash
curl http://localhost:8080/api/locations
```

### Shortest Route

```bash
curl "http://localhost:8080/api/route?from=CP&to=IGI"
curl "http://localhost:8080/api/route?from=CP&to=IGI&algo=dijkstra"
curl "http://localhost:8080/api/route?from=CP&to=IGI&algo=astar"
```

### K-Shortest Routes

```bash
curl "http://localhost:8080/api/route/k?from=CP&to=IGI&k=3"
```

### Benchmark Dijkstra and A*

```bash
curl "http://localhost:8080/api/route/benchmark?from=CP&to=IGI"
```

### Toggle Road Closure

```bash
curl -X POST "http://localhost:8080/api/road/close?from=CP&to=INA&closed=true"
curl -X POST "http://localhost:8080/api/road/close?from=CP&to=INA&closed=false"
```

## Running the Standalone CLI

From the CLI module:

```bash
cd /Users/archita/Documents/Projects/route-optimizer/route-optimizer
rm -rf out
javac -d out src/graph/*.java src/algorithms/*.java src/io/*.java src/Main.java src/TestYens.java
java -cp out Main
```

Available CLI actions:

| Option | Action |
| --- | --- |
| 1 | Find route with Dijkstra's algorithm |
| 2 | Find route with A* search |
| 3 | Benchmark Dijkstra vs A* |
| 4 | List all locations |
| 5 | Toggle road closure |
| 6 | Run full demo |
| 0 | Exit |

Run the standalone Yen's K-shortest routes demo:

```bash
java -cp out TestYens
```

## Location IDs

| ID | Location |
| --- | --- |
| CP | Connaught Place |
| DWK | Dwarka Sector 21 |
| HKV | Hauz Khas Village |
| IGI | IGI Airport |
| INA | INA Market |
| KSY | Kashmere Gate |
| LJN | Lajpat Nagar |
| NDL | New Delhi Station |
| NFC | Nehru Place |
| SKV | Saket |

## Algorithm Notes

### Graph Representation

The road network is stored as an adjacency list. This is appropriate for city maps because road networks are sparse: each location connects to a small number of neighboring locations rather than every other location.

- Space complexity: `O(V + E)`
- Efficient neighbor traversal for routing algorithms
- Supports directed and bidirectional roads

### Dijkstra's Algorithm

Dijkstra's algorithm finds the shortest weighted path from a source to a destination. It is used as the default routing algorithm because road weights are non-negative and represent distance or travel cost.

Time complexity with a priority queue:

```text
O((V + E) log V)
```

### A* Search

A* uses the same weighted graph but prioritizes nodes with:

```text
f(n) = g(n) + h(n)
```

where `g(n)` is the cost from the source to the current node and `h(n)` is the haversine distance from the current node to the destination. This keeps the route optimal while usually exploring fewer nodes than Dijkstra.

### Yen's K-Shortest Paths

Yen's algorithm returns multiple loopless route alternatives between the same source and destination. This is useful for route recommendation where users may want backup routes in case of closures or congestion.

### Road Closures

Each edge stores an `active` flag. Closed roads are skipped during shortest path calculation, allowing route recomputation without rebuilding the graph.

## Example API Response

Request:

```bash
curl "http://localhost:8080/api/route?from=CP&to=IGI&algo=astar"
```

Response:

```json
{
  "path": [
    {
      "id": "CP",
      "name": "Connaught Place",
      "lat": 28.6315,
      "lng": 77.2167
    },
    {
      "id": "INA",
      "name": "INA Market",
      "lat": 28.5733,
      "lng": 77.209
    },
    {
      "id": "IGI",
      "name": "IGI Airport",
      "lat": 28.5562,
      "lng": 77.1
    }
  ],
  "totalDistanceKm": 19.9,
  "nodesExplored": 4,
  "algorithm": "A*"
}
```

## Verification

The project has been verified with:

```bash
cd /Users/archita/Documents/Projects/route-optimizer/route-optimizer-api
mvn test
mvn package
```

```bash
cd /Users/archita/Documents/Projects/route-optimizer/route-optimizer
rm -rf out
javac -d out src/graph/*.java src/algorithms/*.java src/io/*.java src/Main.java src/TestYens.java
java -cp out TestYens
```

## Author

Built by Archita, B.Tech CS and AI, IGDTUW.
