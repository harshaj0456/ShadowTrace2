package SharedModel;

import java.util.*;

/*
 * SpreadSimulator coordinates the malware spread analysis.
 *
 * It uses:
 * - NetworkTopology for network structure
 * - TrustWeightedBFS for compromise propagation
 * - SegmentTracker for compromised segment grouping
 *
 * This class is used by Main.java and threat scoring modules.
 */
public class SpreadSimulator {

    private NetworkTopology networkTopology;
    private TrustWeightedBFS trustWeightedBfs;
    private SegmentTracker segmentTracker;

    // Public results so that scoring/reporting modules can read them directly
    public Map<Integer, Integer> infectionTimeline;
    public List<Integer> fastestSpreadPath;
    public Set<Integer> compromisedNodeIds;
    public Set<Integer> cleanNodeIds;

    public SpreadSimulator(NetworkTopology networkTopology) {
        this.networkTopology = networkTopology;
        this.trustWeightedBfs = new TrustWeightedBFS(networkTopology);
        this.segmentTracker = new SegmentTracker(networkTopology.getAllNodeIds());

        this.infectionTimeline = new HashMap<>();
        this.fastestSpreadPath = new ArrayList<>();
        this.compromisedNodeIds = new HashSet<>();
        this.cleanNodeIds = new HashSet<>();
    }

    /*
     * Runs the full spread simulation from the given entry point.
     */
    public void simulate(int entryPointNodeId) {
        System.out.println("Starting spread simulation from node: " + entryPointNodeId);

        infectionTimeline = trustWeightedBfs.simulateSpread(entryPointNodeId);
        fastestSpreadPath = trustWeightedBfs.findFastestSpreadPath(entryPointNodeId);
        compromisedNodeIds = new HashSet<>(infectionTimeline.keySet());

        cleanNodeIds = segmentTracker.getCleanNodes(networkTopology, compromisedNodeIds);
        segmentTracker.buildCompromisedSegments(compromisedNodeIds);

        System.out.println("Compromised nodes : " + compromisedNodeIds.size());
        System.out.println("Clean nodes       : " + cleanNodeIds.size());
        System.out.println("Fastest spread path: " + fastestSpreadPath);
    }

    /*
     * Packages spread analysis into a ThreatEvent.
     */
    public ThreatEvent getSpreadThreatEvent() {
        return new ThreatEvent(
            "SPREAD",
            new ArrayList<>(compromisedNodeIds),
            90.0,
            "Backdoor spread reached " + compromisedNodeIds.size() +
            " nodes. Fastest spread path: " + fastestSpreadPath
        );
    }
}