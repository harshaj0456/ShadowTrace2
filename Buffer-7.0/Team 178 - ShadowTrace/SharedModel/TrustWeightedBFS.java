package SharedModel;

import java.util.*;

/*
 * TrustWeightedBFS simulates how compromise spreads through the network.
 * It prioritizes spread through more trusted/stronger edges first.
 *
 * It also finds the fastest attack spread path using a Dijkstra-style approach.
 *
 * Used by SpreadSimulator.
 */
public class TrustWeightedBFS {

    private static final double SPREAD_THRESHOLD = 0.30;

    private NetworkTopology networkTopology;

    /*
     * Helper class for spread simulation priority queue.
     */
    private static class SpreadState {
        int nodeId;
        int infectionTime;

        SpreadState(int nodeId, int infectionTime) {
            this.nodeId = nodeId;
            this.infectionTime = infectionTime;
        }
    }

    /*
     * Helper class for shortest path priority queue.
     */
    private static class PathState {
        int nodeId;
        double totalCost;

        PathState(int nodeId, double totalCost) {
            this.nodeId = nodeId;
            this.totalCost = totalCost;
        }
    }

    public TrustWeightedBFS(NetworkTopology networkTopology) {
        this.networkTopology = networkTopology;
    }

    /*
     * Simulates compromise spread from the given entry point.
     * Returns node ID -> infection time map.
     */
    public Map<Integer, Integer> simulateSpread(int entryPointNodeId) {
        Map<Integer, Integer> infectionTimeline = new HashMap<>();

        if (networkTopology.getNode(entryPointNodeId) == null) {
            return infectionTimeline;
        }

        PriorityQueue<SpreadState> priorityQueue = new PriorityQueue<>(
            (first, second) -> {
                NetworkNode firstNode = networkTopology.getNode(first.nodeId);
                NetworkNode secondNode = networkTopology.getNode(second.nodeId);
                return Double.compare(secondNode.trustScore, firstNode.trustScore);
            }
        );

        infectionTimeline.put(entryPointNodeId, 0);
        networkTopology.getNode(entryPointNodeId).isCompromised = true;
        priorityQueue.offer(new SpreadState(entryPointNodeId, 0));

        while (!priorityQueue.isEmpty()) {
            SpreadState currentState = priorityQueue.poll();
            int currentNodeId = currentState.nodeId;
            int currentInfectionTime = currentState.infectionTime;

            for (NetworkEdge edge : networkTopology.getNeighbors(currentNodeId)) {
                int neighborNodeId = edge.destinationNodeId;

                if (!infectionTimeline.containsKey(neighborNodeId) &&
                    edge.weight > SPREAD_THRESHOLD) {

                    infectionTimeline.put(neighborNodeId, currentInfectionTime + 1);
                    networkTopology.getNode(neighborNodeId).isCompromised = true;
                    priorityQueue.offer(new SpreadState(neighborNodeId, currentInfectionTime + 1));
                }
            }
        }

        return infectionTimeline;
    }

    /*
     * Finds the fastest spread path from the entry point
     * to the most sensitive compromised node.
     */
    public List<Integer> findFastestSpreadPath(int entryPointNodeId) {
        Map<Integer, Double> shortestDistance = new HashMap<>();
        Map<Integer, Integer> previousNode = new HashMap<>();

        PriorityQueue<PathState> priorityQueue = new PriorityQueue<>(
            Comparator.comparingDouble(state -> state.totalCost)
        );

        for (int nodeId : networkTopology.getAllNodeIds()) {
            shortestDistance.put(nodeId, Double.MAX_VALUE);
        }

        shortestDistance.put(entryPointNodeId, 0.0);
        priorityQueue.offer(new PathState(entryPointNodeId, 0.0));

        while (!priorityQueue.isEmpty()) {
            PathState currentState = priorityQueue.poll();
            int currentNodeId = currentState.nodeId;

            if (currentState.totalCost > shortestDistance.get(currentNodeId)) {
                continue;
            }

            for (NetworkEdge edge : networkTopology.getNeighbors(currentNodeId)) {
                int neighborNodeId = edge.destinationNodeId;
                double edgeCost = 1.0 - edge.weight;
                double newDistance = shortestDistance.get(currentNodeId) + edgeCost;

                if (newDistance < shortestDistance.get(neighborNodeId)) {
                    shortestDistance.put(neighborNodeId, newDistance);
                    previousNode.put(neighborNodeId, currentNodeId);
                    priorityQueue.offer(new PathState(neighborNodeId, newDistance));
                }
            }
        }

        // Find most sensitive compromised node (lowest trust score)
        int targetNodeId = entryPointNodeId;
        double lowestTrustScore = Double.MAX_VALUE;

        for (int nodeId : networkTopology.getAllNodeIds()) {
            NetworkNode currentNode = networkTopology.getNode(nodeId);

            if (currentNode.isCompromised &&
                nodeId != entryPointNodeId &&
                currentNode.trustScore < lowestTrustScore) {

                lowestTrustScore = currentNode.trustScore;
                targetNodeId = nodeId;
            }
        }

        List<Integer> fastestPath = new ArrayList<>();
        Integer currentNodeId = targetNodeId;

        while (currentNodeId != null) {
            fastestPath.add(0, currentNodeId);
            currentNodeId = previousNode.get(currentNodeId);
        }

        return fastestPath;
    }
}