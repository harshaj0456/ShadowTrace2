package SharedModel;

import java.util.*;

/*
 * TopologicalSorter generates a safe removal order
 * for persistence-related malicious nodes.
 *
 * It uses a Kahn-style topological approach so that
 * dependent nodes are removed in a safer sequence.
 *
 * Used by RemovalEngine.
 */
public class TopologicalSorter {

    /*
     * Returns a dependency-safe removal order for the given cycle nodes.
     */
    public List<Integer> getSafeRemovalOrder(List<Integer> cycleNodeIds,
                                             DependencyGraph dependencyGraph) {
        Map<Integer, Integer> inDegreeMap = new HashMap<>();

        for (int nodeId : cycleNodeIds) {
            inDegreeMap.put(nodeId, 0);
        }

        // Count incoming edges between nodes inside the cycle set
        for (int nodeId : cycleNodeIds) {
            for (NetworkEdge edge : dependencyGraph.getNeighbors(nodeId)) {
                int neighborNodeId = edge.destinationNodeId;

                if (inDegreeMap.containsKey(neighborNodeId)) {
                    inDegreeMap.put(
                        neighborNodeId,
                        inDegreeMap.get(neighborNodeId) + 1
                    );
                }
            }
        }

        Queue<Integer> zeroInDegreeQueue = new LinkedList<>();
        for (Map.Entry<Integer, Integer> entry : inDegreeMap.entrySet()) {
            if (entry.getValue() == 0) {
                zeroInDegreeQueue.add(entry.getKey());
            }
        }

        // If this is a pure cycle, force-break from first node
        if (zeroInDegreeQueue.isEmpty() && !cycleNodeIds.isEmpty()) {
            int forcedStartNodeId = cycleNodeIds.get(0);
            zeroInDegreeQueue.add(forcedStartNodeId);
            inDegreeMap.put(forcedStartNodeId, 0);
        }

        List<Integer> safeRemovalOrder = new ArrayList<>();
        Set<Integer> scheduledNodeIds = new HashSet<>();

        while (!zeroInDegreeQueue.isEmpty()) {
            int currentNodeId = zeroInDegreeQueue.poll();

            if (scheduledNodeIds.contains(currentNodeId)) {
                continue;
            }

            safeRemovalOrder.add(currentNodeId);
            scheduledNodeIds.add(currentNodeId);

            for (NetworkEdge edge : dependencyGraph.getNeighbors(currentNodeId)) {
                int neighborNodeId = edge.destinationNodeId;

                if (inDegreeMap.containsKey(neighborNodeId)) {
                    int newInDegree = inDegreeMap.get(neighborNodeId) - 1;
                    inDegreeMap.put(neighborNodeId, newInDegree);

                    if (newInDegree == 0) {
                        zeroInDegreeQueue.add(neighborNodeId);
                    }
                }
            }
        }

        // Safety fallback: append unscheduled cycle nodes
        for (int nodeId : cycleNodeIds) {
            if (!scheduledNodeIds.contains(nodeId)) {
                safeRemovalOrder.add(nodeId);
            }
        }

        return safeRemovalOrder;
    }
}