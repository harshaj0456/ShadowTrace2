package SharedModel;

import java.util.*;

public class ScoringDemo {
    public static void main(String[] args) {

        List<ThreatEvent> allThreatEvents = new ArrayList<>();

        allThreatEvents.add(new ThreatEvent(
            "PERSISTENCE_CYCLE",
            Arrays.asList(1, 2, 3),
            100.0,
            "3-node backdoor cycle detected"
        ));

        allThreatEvents.add(new ThreatEvent(
            "MITM",
            Arrays.asList(7),
            90.0,
            "ARP spoofing detected on node 7"
        ));

        allThreatEvents.add(new ThreatEvent(
            "SPREAD",
            Arrays.asList(1, 2, 3, 4, 5, 6, 7),
            90.0,
            "Backdoor spread to 7 nodes. Fastest attack path: [5, 1, 2, 7]"
        ));

        Set<Integer> compromisedNodeIds = new HashSet<>(
            Arrays.asList(1, 2, 3, 4, 5, 6, 7)
        );

        System.out.println("=== ADDING THREATS TO HEAP ===");
        RiskHeap riskHeap = new RiskHeap();

        for (ThreatEvent threatEvent : allThreatEvents) {
            riskHeap.addThreat(threatEvent, compromisedNodeIds);
        }

        System.out.println("\n=== RISK RANKING (highest risk first) ===");
        for (ThreatEvent threatEvent : riskHeap.getOrderedThreats()) {
            System.out.printf("%-20s | score=%5.1f | %s%n",
                threatEvent.threatType,
                threatEvent.confidenceScore,
                threatEvent.description);
        }

        ThreatEvent highestRiskThreat = riskHeap.getHighestRiskThreat();
        if (highestRiskThreat != null) {
            System.out.println("\nHighest risk threat: " +
                highestRiskThreat.threatType);
        }

        DependencyGraph dependencyGraph = new DependencyGraph();
        dependencyGraph.addNode(new NetworkNode(1, "ScheduledTask_A", "task", 0.0));
        dependencyGraph.addNode(new NetworkNode(2, "RegistryKey_B", "registry", 0.0));
        dependencyGraph.addNode(new NetworkNode(3, "RogueProcess_C", "process", 0.0));

        dependencyGraph.addDirectedEdge(new NetworkEdge(1, 2, 1.0, "reinstalls"));
        dependencyGraph.addDirectedEdge(new NetworkEdge(2, 3, 1.0, "reinstalls"));
        dependencyGraph.addDirectedEdge(new NetworkEdge(3, 1, 0.3, "reinstalls"));

        RemovalEngine removalEngine = new RemovalEngine();
        List<String> removalPlan = removalEngine.computeRemovalPlan(
            Arrays.asList(allThreatEvents.get(0)),
            dependencyGraph
        );

        System.out.println("\n=== SAFE REMOVAL PLAN ===");
        for (String step : removalPlan) {
            System.out.println(step);
        }
    }
}