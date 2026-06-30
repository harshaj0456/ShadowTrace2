package SharedModel;

/*
 * NetworkNode represents a single entity in the system.
 * It can be a device, process, registry key, task, etc.
 *
 * This is a core model class used by:
 * - NetworkGraph
 * - DependencyGraph
 * - NetworkTopology
 * - MITM detection
 * - Persistence detection
 * - Spread simulation
 */
public class NetworkNode {

    // Unique identifier of the node
    public int nodeId;

    // Human-readable name of the node
    public String nodeName;

    // Type of node: device / process / registry / task / account
    public String nodeType;

    // Trust score of the node (0.0 to 1.0)
    public double trustScore;

    // Tracks whether the node is compromised during spread simulation
    public boolean isCompromised;

    /*
     * Creates a new network node.
     */
    public NetworkNode(int nodeId, String nodeName, String nodeType, double trustScore) {
        this.nodeId = nodeId;
        this.nodeName = nodeName;
        this.nodeType = nodeType;
        this.trustScore = trustScore;
        this.isCompromised = false;
    }
}