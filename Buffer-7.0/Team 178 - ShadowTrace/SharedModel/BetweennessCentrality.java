package SharedModel;

import java.util.*;

/*
 * BetweennessCentrality estimates the most influential node
 * in the dependency graph and uses it as a likely entry point.
 *
 * A node with high betweenness lies on many shortest paths,
 * making it structurally important in graph traversal.
 */
public class BetweennessCentrality {

    private DependencyGraph dependencyGraph;

    public BetweennessCentrality(DependencyGraph dependencyGraph) {
        this.dependencyGraph = dependencyGraph;
    }

    /*
     * Finds the node with highest centrality score.
     */
    public int findEntryPoint() {
        Map<Integer, Double> centralityScores = computeCentrality();

        int likelyEntryPoint = -1;
        double highestScore = -1.0;

        for (Map.Entry<Integer, Double> entry : centralityScores.entrySet()) {
            if (entry.getValue() > highestScore) {
                highestScore = entry.getValue();
                likelyEntryPoint = entry.getKey();
            }
        }

        System.out.println("Likely entry point: Node " + likelyEntryPoint +
                           " (score = " + highestScore + ")");
        return likelyEntryPoint;
    }

    /*
     * Computes centrality score for all nodes.
     */
    private Map<Integer, Double> computeCentrality() {
        Map<Integer, Double> centralityScores = new HashMap<>();

        for (int nodeId : dependencyGraph.getAllNodeIds()) {
            centralityScores.put(nodeId, 0.0);
        }

        for (int sourceNodeId : dependencyGraph.getAllNodeIds()) {

            Stack<Integer> dfsOrderStack = new Stack<>();
            Map<Integer, List<Integer>> predecessorMap = new HashMap<>();
            Map<Integer, Double> shortestPathCount = new HashMap<>();
            Map<Integer, Integer> distanceMap = new HashMap<>();

            for (int nodeId : dependencyGraph.getAllNodeIds()) {
                predecessorMap.put(nodeId, new ArrayList<>());
                shortestPathCount.put(nodeId, 0.0);
                distanceMap.put(nodeId, -1);
            }

            shortestPathCount.put(sourceNodeId, 1.0);
            distanceMap.put(sourceNodeId, 0);

            Queue<Integer> bfsQueue = new LinkedList<>();
            bfsQueue.add(sourceNodeId);

            while (!bfsQueue.isEmpty()) {
                int currentNodeId = bfsQueue.poll();
                dfsOrderStack.push(currentNodeId);

                for (NetworkEdge edge : dependencyGraph.getNeighbors(currentNodeId)) {
                    int neighborNodeId = edge.destinationNodeId;

                    if (distanceMap.get(neighborNodeId) < 0) {
                        bfsQueue.add(neighborNodeId);
                        distanceMap.put(neighborNodeId, distanceMap.get(currentNodeId) + 1);
                    }

                    if (distanceMap.get(neighborNodeId) == distanceMap.get(currentNodeId) + 1) {
                        shortestPathCount.put(
                            neighborNodeId,
                            shortestPathCount.get(neighborNodeId) + shortestPathCount.get(currentNodeId)
                        );
                        predecessorMap.get(neighborNodeId).add(currentNodeId);
                    }
                }
            }

            Map<Integer, Double> dependencyScore = new HashMap<>();
            for (int nodeId : dependencyGraph.getAllNodeIds()) {
                dependencyScore.put(nodeId, 0.0);
            }

            while (!dfsOrderStack.isEmpty()) {
                int currentNodeId = dfsOrderStack.pop();

                for (int predecessorNodeId : predecessorMap.get(currentNodeId)) {
                    double contribution =
                        (shortestPathCount.get(predecessorNodeId) / shortestPathCount.get(currentNodeId)) *
                        (1.0 + dependencyScore.get(currentNodeId));

                    dependencyScore.put(
                        predecessorNodeId,
                        dependencyScore.get(predecessorNodeId) + contribution
                    );
                }

                if (currentNodeId != sourceNodeId) {
                    centralityScores.put(
                        currentNodeId,
                        centralityScores.get(currentNodeId) + dependencyScore.get(currentNodeId)
                    );
                }
            }
        }

        return centralityScores;
    }
}