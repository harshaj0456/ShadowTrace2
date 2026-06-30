package SharedModel;

import java.util.*;

/*
 * NetworkTopology represents the network used for spread simulation.
 * It is an undirected weighted graph.
 *
 * Used in:
 * - TrustWeightedBFS
 * - SpreadSimulator
 * - ThreatScorer
 */
public class NetworkTopology {

    // Stores all connected edges for each node
    private Map<Integer, List<NetworkEdge>> adjacencyList;

    // Stores node details by node ID
    private Map<Integer, NetworkNode> nodes;

    public NetworkTopology() {
        adjacencyList = new HashMap<>();
        nodes = new HashMap<>();
    }

    /*
     * Adds a node to the topology.
     */
    public boolean addNode(NetworkNode node) {
        if (node == null || nodes.containsKey(node.nodeId)) {
            return false;
        }

        nodes.put(node.nodeId, node);
        adjacencyList.put(node.nodeId, new ArrayList<>());
        return true;
    }

    /*
     * Adds an undirected edge to the topology.
     * Both forward and reverse edges are stored.
     */
    public boolean addEdge(NetworkEdge edge) {
        if (edge == null) {
            return false;
        }

        if (!nodes.containsKey(edge.sourceNodeId) || !nodes.containsKey(edge.destinationNodeId)) {
            return false;
        }

        adjacencyList.get(edge.sourceNodeId).add(edge);

        NetworkEdge reverseEdge = new NetworkEdge(
            edge.destinationNodeId,
            edge.sourceNodeId,
            edge.weight,
            edge.edgeType
        );
        adjacencyList.get(edge.destinationNodeId).add(reverseEdge);

        return true;
    }

    /*
     * Returns neighboring edges of the node.
     */
    public List<NetworkEdge> getNeighbors(int nodeId) {
        return adjacencyList.getOrDefault(nodeId, new ArrayList<>());
    }

    /*
     * Returns node object by node ID.
     */
    public NetworkNode getNode(int nodeId) {
        return nodes.get(nodeId);
    }

    /*
     * Returns all node IDs in the topology.
     */
    public Set<Integer> getAllNodeIds() {
        return nodes.keySet();
    }

    /*
     * Prints topology for debugging/demo.
     */
    public void printTopology() {
        System.out.println("=== Network Topology ===");
        for (int nodeId : adjacencyList.keySet()) {
            System.out.print(nodes.get(nodeId).nodeName + " -> ");
            for (NetworkEdge edge : adjacencyList.get(nodeId)) {
                System.out.print(nodes.get(edge.destinationNodeId).nodeName + "(w=" + edge.weight + ") ");
            }
            System.out.println();
        }
    }
}