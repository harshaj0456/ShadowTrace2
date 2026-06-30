package SharedModel;

import java.util.*;

public class SpreadDemo {
    public static void main(String[] args) {

        NetworkTopology networkTopology = new NetworkTopology();

        networkTopology.addNode(new NetworkNode(1, "Server_1", "device", 0.9));
        networkTopology.addNode(new NetworkNode(2, "Server_2", "device", 0.8));
        networkTopology.addNode(new NetworkNode(3, "Workstation_A", "device", 0.7));
        networkTopology.addNode(new NetworkNode(4, "Workstation_B", "device", 0.6));
        networkTopology.addNode(new NetworkNode(5, "Entry_Node", "device", 0.5));
        networkTopology.addNode(new NetworkNode(6, "Database", "device", 0.9));
        networkTopology.addNode(new NetworkNode(7, "Backup", "device", 0.4));
        networkTopology.addNode(new NetworkNode(8, "Isolated", "device", 0.1));

        networkTopology.addEdge(new NetworkEdge(5, 1, 0.9, "network"));
        networkTopology.addEdge(new NetworkEdge(5, 3, 0.7, "network"));
        networkTopology.addEdge(new NetworkEdge(1, 2, 0.8, "network"));
        networkTopology.addEdge(new NetworkEdge(1, 6, 0.9, "network"));
        networkTopology.addEdge(new NetworkEdge(3, 4, 0.6, "network"));
        networkTopology.addEdge(new NetworkEdge(2, 7, 0.4, "network"));
        networkTopology.addEdge(new NetworkEdge(8, 7, 0.2, "network"));

        int entryPointNodeId = 5;

        SpreadSimulator spreadSimulator = new SpreadSimulator(networkTopology);
        spreadSimulator.simulate(entryPointNodeId);

        System.out.println("\n=== INFECTION TIMELINE ===");
        spreadSimulator.infectionTimeline.entrySet()
            .stream()
            .sorted(Map.Entry.comparingByValue())
            .forEach(entry -> System.out.println(
                "Node " + entry.getKey() + " infected at T=" + entry.getValue()
            ));

        System.out.println("\n=== CLEAN NODES ===");
        System.out.println(spreadSimulator.cleanNodeIds);

        System.out.println("\n=== THREAT EVENT (passed to IT 1) ===");
        ThreatEvent spreadThreatEvent = spreadSimulator.getSpreadThreatEvent();
        System.out.println("Type        : " + spreadThreatEvent.threatType);
        System.out.println("Confidence  : " + spreadThreatEvent.confidenceScore);
        System.out.println("Description : " + spreadThreatEvent.description);
        System.out.println("Nodes       : " + spreadThreatEvent.affectedNodeIds);
    }
}