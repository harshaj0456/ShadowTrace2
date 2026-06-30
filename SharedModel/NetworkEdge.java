package SharedModel;

/*
 * NetworkEdge represents a connection between two nodes.
 *
 * It is used in:
 * - NetworkGraph for communication links
 * - DependencyGraph for directed dependency/reinstallation links
 * - NetworkTopology for spread simulation
 */
public class NetworkEdge {

    // Source node of the edge
    public int sourceNodeId;

    // Destination node of the edge
    public int destinationNodeId;

    // Weight of the edge
    // In communication graph: trust strength
    // In dependency graph: relation strength / importance
    public double weight;

    // Type of relation: network / reinstalls / depends_on / dependency
    public String edgeType;

    /*
     * Creates a new edge between two nodes.
     */
    public NetworkEdge(int sourceNodeId, int destinationNodeId, double weight, String edgeType) {
        this.sourceNodeId = sourceNodeId;
        this.destinationNodeId = destinationNodeId;
        this.weight = weight;
        this.edgeType = edgeType;
    }
}