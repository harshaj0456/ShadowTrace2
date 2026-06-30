package SharedModel;

import java.util.*;

/*
 * DijkstraPathFinder finds the expected safest path
 * between two nodes in the communication network.
 *
 * It is used by MITMDetector to compare:
 * - expected safe path
 * - actual suspicious path
 *
 * Edge cost is calculated as:
 * cost = 1.0 - trustScore
 * So higher trust means lower cost.
 */
public class DijkstraPathFinder {

    private NetworkGraph networkGraph;

    /*
     * Helper class for priority queue.
     * Stores current node and current known path cost.
     */
    private static class PathState {
        int nodeId;
        double totalCost;

        PathState(int nodeId, double totalCost) {
            this.nodeId = nodeId;
            this.totalCost = totalCost;
        }
    }

    public DijkstraPathFinder(NetworkGraph networkGraph) {
        this.networkGraph = networkGraph;
    }

    /*
     * Finds the safest expected path between source and destination.
     * Returns list of node IDs in order.
     */
    public List<Integer> findShortestPath(int sourceNodeId, int destinationNodeId) {
        Map<Integer, Double> shortestDistance = new HashMap<>();
        Map<Integer, Integer> previousNode = new HashMap<>();

        PriorityQueue<PathState> priorityQueue = new PriorityQueue<>(
            Comparator.comparingDouble(state -> state.totalCost)
        );

        for (int nodeId : networkGraph.getAllNodeIds()) {
            shortestDistance.put(nodeId, Double.MAX_VALUE);
        }

        shortestDistance.put(sourceNodeId, 0.0);
        priorityQueue.offer(new PathState(sourceNodeId, 0.0));

        while (!priorityQueue.isEmpty()) {
            PathState currentState = priorityQueue.poll();
            int currentNodeId = currentState.nodeId;

            if (currentState.totalCost > shortestDistance.get(currentNodeId)) {
                continue;
            }

            if (currentNodeId == destinationNodeId) {
                break;
            }

            for (NetworkEdge edge : networkGraph.getNeighbors(currentNodeId)) {
                int neighborNodeId = edge.destinationNodeId;

                // Lower trust means higher cost
                double edgeCost = 1.0 - edge.weight;
                double newDistance = shortestDistance.get(currentNodeId) + edgeCost;

                if (newDistance < shortestDistance.get(neighborNodeId)) {
                    shortestDistance.put(neighborNodeId, newDistance);
                    previousNode.put(neighborNodeId, currentNodeId);
                    priorityQueue.offer(new PathState(neighborNodeId, newDistance));
                }
            }
        }

        if (sourceNodeId == destinationNodeId) {
            return Arrays.asList(sourceNodeId);
        }

        if (!previousNode.containsKey(destinationNodeId)) {
            return new ArrayList<>();
        }

        List<Integer> path = new ArrayList<>();
        Integer currentNodeId = destinationNodeId;

        while (currentNodeId != null) {
            path.add(0, currentNodeId);
            currentNodeId = previousNode.get(currentNodeId);
        }

        return path;
    }
}