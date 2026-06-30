package SharedModel;

import java.util.*;

/*
 * MITMDetector identifies possible Man-in-the-Middle attacks using:
 * 1. Path deviation analysis
 * 2. ARP spoofing analysis
 *
 * It compares the expected safest path against the actual observed path.
 * Unexpected intermediate nodes may indicate interception.
 *
 * It also uses ARPCacheMonitor to detect suspicious ARP behavior.
 */
public class MITMDetector 
{

    private NetworkGraph networkGraph;
    private DijkstraPathFinder pathFinder;
    private ARPCacheMonitor arpCacheMonitor;
    private List<ThreatEvent> pathDeviationThreats;

    public MITMDetector(NetworkGraph networkGraph) 
    {
        this.networkGraph = networkGraph;
        this.pathFinder = new DijkstraPathFinder(networkGraph);
        this.arpCacheMonitor = new ARPCacheMonitor();
        this.pathDeviationThreats = new ArrayList<>();
    }

    /*
     * Compares expected safe path and actual observed path.
     * If unexpected nodes appear in the actual path,
     * they are treated as suspicious.
     */
    public void analyzePathDeviation(int sourceNodeId, int destinationNodeId,
                                     List<Integer> actualObservedPath) {

        List<Integer> expectedPath = pathFinder.findShortestPath(sourceNodeId, destinationNodeId);

        System.out.println("Expected Path : " + expectedPath);
        System.out.println("Actual Path   : " + actualObservedPath);

        Set<Integer> expectedPathNodeSet = new HashSet<>(expectedPath);
        List<Integer> suspiciousNodeIds = new ArrayList<>();

        for (int nodeId : actualObservedPath) {
            if (!expectedPathNodeSet.contains(nodeId)) {
                suspiciousNodeIds.add(nodeId);
            }
        }

        if (!suspiciousNodeIds.isEmpty()) {
            double confidenceScore = 70.0 + (suspiciousNodeIds.size() * 10.0);

            ThreatEvent mitmThreat = new ThreatEvent(
                "MITM",
                suspiciousNodeIds,
                Math.min(confidenceScore, 100.0),
                "Path deviation detected. Unexpected nodes found in actual path: " + suspiciousNodeIds
            );

            pathDeviationThreats.add(mitmThreat);
            System.out.println("MITM DETECTED: " + mitmThreat.description);
        } else {
            System.out.println("No path deviation detected.");
        }
    }

    /*
     * Passes observed ARP entry to ARP monitor.
     */
    public void loadARPEntry(String ipAddress, String macAddress, int reportingNodeId) {
        arpCacheMonitor.observeARPEntry(ipAddress, macAddress, reportingNodeId);
    }

    /*
     * Loads a legitimate ARP mapping into ARP monitor.
     */
    public void loadLegitimateARP(String ipAddress, String macAddress) {
        arpCacheMonitor.loadLegitimateARP(ipAddress, macAddress);
    }

    /*
     * Returns all MITM-related threats collected so far.
     * This includes both path-deviation and ARP-based threats.
     */
    public List<ThreatEvent> getAllThreats() {
        List<ThreatEvent> combinedThreats = new ArrayList<>();
        combinedThreats.addAll(pathDeviationThreats);
        combinedThreats.addAll(arpCacheMonitor.getDetectedThreats());
        return combinedThreats;
    }
}