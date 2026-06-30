package SharedModel;

import java.util.*;

/*
 * SegmentTracker uses Disjoint Set Union (Union-Find)
 * to group compromised nodes into the same segment.
 *
 * It helps separate compromised and clean parts of the network.
 *
 * Used by SpreadSimulator.
 */
public class SegmentTracker {

    private Map<Integer, Integer> parentNode;
    private Map<Integer, Integer> rank;

    public SegmentTracker(Set<Integer> nodeIds) {
        parentNode = new HashMap<>();
        rank = new HashMap<>();

        for (int nodeId : nodeIds) {
            parentNode.put(nodeId, nodeId);
            rank.put(nodeId, 0);
        }
    }

    /*
     * Finds the segment representative of a node.
     * Uses path compression for optimization.
     */
    public int find(int nodeId) {
        if (parentNode.get(nodeId) != nodeId) {
            parentNode.put(nodeId, find(parentNode.get(nodeId)));
        }
        return parentNode.get(nodeId);
    }

    /*
     * Unions two segments using union by rank.
     */
    public void union(int firstNodeId, int secondNodeId) {
        int firstRoot = find(firstNodeId);
        int secondRoot = find(secondNodeId);

        if (firstRoot == secondRoot) {
            return;
        }

        if (rank.get(firstRoot) < rank.get(secondRoot)) {
            parentNode.put(firstRoot, secondRoot);
        } else if (rank.get(firstRoot) > rank.get(secondRoot)) {
            parentNode.put(secondRoot, firstRoot);
        } else {
            parentNode.put(secondRoot, firstRoot);
            rank.put(firstRoot, rank.get(firstRoot) + 1);
        }
    }

    /*
     * Returns true if both nodes belong to the same segment.
     */
    public boolean sameSegment(int firstNodeId, int secondNodeId) {
        return find(firstNodeId) == find(secondNodeId);
    }

    /*
     * Groups all compromised nodes into one segment.
     */
    public void buildCompromisedSegments(Set<Integer> compromisedNodeIds) {
        List<Integer> compromisedNodeList = new ArrayList<>(compromisedNodeIds);

        for (int index = 1; index < compromisedNodeList.size(); index++) {
            union(compromisedNodeList.get(0), compromisedNodeList.get(index));
        }
    }

    /*
     * Returns all clean nodes in the topology.
     */
    public Set<Integer> getCleanNodes(NetworkTopology networkTopology, Set<Integer> compromisedNodeIds) {
        Set<Integer> cleanNodeIds = new HashSet<>(networkTopology.getAllNodeIds());
        cleanNodeIds.removeAll(compromisedNodeIds);
        return cleanNodeIds;
    }
}