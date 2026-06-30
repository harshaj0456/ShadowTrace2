package SharedModel;

import java.util.*;

/*
 * DependencyGraph represents directed dependency relationships
 * between components such as processes, registry keys, scripts, etc.
 *
 * Used in:
 * - TarjanSCC
 * - ArticulationPointFinder
 * - BetweennessCentrality
 */
public class DependencyGraph {

    // Stores all outgoing dependency edges
    private Map<Integer, List<NetworkEdge>> adjacencyList;

    // Stores node details
    private Map<Integer, NetworkNode> nodes;

    // Tracks number of unique nodes
    private int nodeCount;

    public DependencyGraph() {
        adjacencyList = new HashMap<>();
        nodes = new HashMap<>();
        nodeCount = 0;
    }

    /*
     * Adds a node to the dependency graph.
     */
    public boolean addNode(NetworkNode node) {
        if (node == null || nodes.containsKey(node.nodeId)) {
            return false;
        }

        nodes.put(node.nodeId, node);
        adjacencyList.put(node.nodeId, new ArrayList<>());
        nodeCount++;
        return true;
    }

    /*
     * Adds a directed edge to the dependency graph.
     * Example: A -> B means A depends on or reinstalls B.
     */
    public boolean addDirectedEdge(NetworkEdge edge) {
        if (edge == null) {
            return false;
        }

        if (!nodes.containsKey(edge.sourceNodeId) || !nodes.containsKey(edge.destinationNodeId)) {
            return false;
        }

        adjacencyList.get(edge.sourceNodeId).add(edge);
        return true;
    }

    /*
     * Returns outgoing neighbors of a node.
     */
    public List<NetworkEdge> getNeighbors(int nodeId) {
        return adjacencyList.getOrDefault(nodeId, new ArrayList<>());
    }

    /*
     * Returns node details by node ID.
     */
    public NetworkNode getNode(int nodeId) {
        return nodes.get(nodeId);
    }

    /*
     * Returns all node IDs.
     */
    public Set<Integer> getAllNodeIds() {
        return nodes.keySet();
    }

    /*
     * Returns number of unique nodes.
     */
    public int getNodeCount() {
        return nodeCount;
    }

    /*
     * Prints dependency graph.
     */
    public void printGraph() {
        System.out.println("=== Dependency Graph ===");
        for (int nodeId : adjacencyList.keySet()) {
            System.out.print(nodes.get(nodeId).nodeName + " -> ");
            for (NetworkEdge edge : adjacencyList.get(nodeId)) {
                System.out.print(nodes.get(edge.destinationNodeId).nodeName + " ");
            }
            System.out.println();
        }
    }
}