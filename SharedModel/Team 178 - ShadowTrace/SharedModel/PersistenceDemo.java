package SharedModel;

import java.util.*;

public class PersistenceDemo {
    public static void main(String[] args) {

        DependencyGraph dependencyGraph = new DependencyGraph();

        dependencyGraph.addNode(new NetworkNode(1, "ScheduledTask_A", "task", 0.0));
        dependencyGraph.addNode(new NetworkNode(2, "RegistryKey_B", "registry", 0.0));
        dependencyGraph.addNode(new NetworkNode(3, "RogueProcess_C", "process", 0.0));
        dependencyGraph.addNode(new NetworkNode(4, "RogueAccount_D", "account", 0.0));
        dependencyGraph.addNode(new NetworkNode(5, "StartupScript_E", "task", 0.0));

        dependencyGraph.addDirectedEdge(new NetworkEdge(1, 2, 1.0, "reinstalls"));
        dependencyGraph.addDirectedEdge(new NetworkEdge(2, 3, 1.0, "reinstalls"));
        dependencyGraph.addDirectedEdge(new NetworkEdge(3, 1, 1.0, "reinstalls"));

        dependencyGraph.addDirectedEdge(new NetworkEdge(4, 1, 1.0, "depends_on"));
        dependencyGraph.addDirectedEdge(new NetworkEdge(5, 2, 1.0, "depends_on"));

        System.out.println("=== TARJAN SCC ===");
        TarjanSCC tarjanScc = new TarjanSCC(dependencyGraph, 5);
        tarjanScc.findAllSCCs();

        for (ThreatEvent threatEvent : tarjanScc.getCyclesAsThreatEvents()) {
            System.out.println("BACKDOOR: " + threatEvent.description);
            System.out.println("Nodes: " + threatEvent.affectedNodeIds);
        }

        System.out.println("\n=== ARTICULATION POINTS ===");
        ArticulationPointFinder articulationPointFinder =
            new ArticulationPointFinder(dependencyGraph, 5);
        Set<Integer> articulationPoints =
            articulationPointFinder.findArticulationPoints();
        System.out.println("Critical nodes: " + articulationPoints);

        System.out.println("\n=== ENTRY POINT ===");
        BetweennessCentrality betweennessCentrality =
            new BetweennessCentrality(dependencyGraph);
        int entryPointNodeId = betweennessCentrality.findEntryPoint();
        System.out.println("Attacker entered through node: " + entryPointNodeId);
    }
}