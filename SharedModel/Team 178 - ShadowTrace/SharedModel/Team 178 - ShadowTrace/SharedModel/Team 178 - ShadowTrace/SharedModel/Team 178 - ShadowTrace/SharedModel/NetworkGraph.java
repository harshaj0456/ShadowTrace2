package SharedModel;

import java.util.*;

/*
 * NetworkGraph represents the communication network of the system.
 * It is an undirected weighted graph:
 * - nodes represent devices or systems
 * - edges represent communication links
 *
 * Used in:
 * - DijkstraPathFinder
 * - MITMDetector
 */
public class NetworkGraph {

    // Stores all communication links for each node
    private Map<Integer, List<NetworkEdge>> adjacencyList;

    // Stores node details using node ID as key
    private Map<Integer, NetworkNode> nodes;

    public NetworkGraph() {
        adjacencyList = new HashMap<>();
        nodes = new HashMap<>();
    }

    /*
     * Adds a node to the graph.
     * Returns true if node is added successfully.
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
     * Adds an undirected edge to the graph.
     * Since the communication network is bidirectional,
     * both forward and reverse edges are added.
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
     * Returns all neighboring edges for a given node.
     */
    public List<NetworkEdge> getNeighbors(int nodeId) {
        return adjacencyList.getOrDefault(nodeId, new ArrayList<>());
    }

    /*
     * Returns the node object for a given node ID.
     */
    public NetworkNode getNode(int nodeId) {
        return nodes.get(nodeId);
    }

    /*
     * Returns all node IDs in the graph.
     */
    public Set<Integer> getAllNodeIds() {
        return nodes.keySet();
    }

    /*
     * Prints the graph structure for debugging/demo.
     */
    public void printGraph() {
        System.out.println("=== Network Graph ===");
        for (int nodeId : adjacencyList.keySet()) {
            System.out.print("Node " + nodes.get(nodeId).nodeName + " -> ");
            for (NetworkEdge edge : adjacencyList.get(nodeId)) {
                System.out.print(nodes.get(edge.destinationNodeId).nodeName + "(w=" + edge.weight + ") ");
            }
            System.out.println();
        }
    }
}