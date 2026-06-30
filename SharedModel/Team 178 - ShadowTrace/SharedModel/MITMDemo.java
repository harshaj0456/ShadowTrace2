package SharedModel;

import java.util.*;

public class MITMDemo {
    public static void main(String[] args) {

        NetworkGraph networkGraph = new NetworkGraph();

        networkGraph.addNode(new NetworkNode(1, "NodeA", "device", 0.9));
        networkGraph.addNode(new NetworkNode(2, "Router", "device", 1.0));
        networkGraph.addNode(new NetworkNode(3, "NodeB", "device", 0.8));
        networkGraph.addNode(new NetworkNode(4, "Attacker", "device", 0.1));

        networkGraph.addEdge(new NetworkEdge(1, 2, 0.9, "network"));
        networkGraph.addEdge(new NetworkEdge(2, 3, 0.8, "network"));
        networkGraph.addEdge(new NetworkEdge(1, 4, 0.6, "network"));
        networkGraph.addEdge(new NetworkEdge(4, 3, 0.6, "network"));

        MITMDetector mitmDetector = new MITMDetector(networkGraph);

        mitmDetector.loadLegitimateARP("192.168.1.1", "AA:BB:CC:DD:EE:01");
        mitmDetector.loadLegitimateARP("192.168.1.2", "AA:BB:CC:DD:EE:02");
        mitmDetector.loadLegitimateARP("192.168.1.3", "AA:BB:CC:DD:EE:03");

        System.out.println("=== ARP MONITORING ===");
        mitmDetector.loadARPEntry("192.168.1.1", "AA:BB:CC:DD:EE:01", 1);
        mitmDetector.loadARPEntry("192.168.1.3", "AA:BB:CC:DD:EE:03", 3);
        mitmDetector.loadARPEntry("192.168.1.2", "AA:BB:CC:DD:EE:01", 4);

        System.out.println("\n=== PATH ANALYSIS ===");
        List<Integer> actualObservedPath = Arrays.asList(1, 4, 3);
        mitmDetector.analyzePathDeviation(1, 3, actualObservedPath);

        System.out.println("\n=== ALL THREATS ===");
        for (ThreatEvent threatEvent : mitmDetector.getAllThreats()) {
            System.out.println("[" + threatEvent.threatType + "] Score: " +
                               threatEvent.confidenceScore + " | " +
                               threatEvent.description);
        }
    }
}