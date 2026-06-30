package SharedModel;

import java.util.*;

/*
 * TarjanSCC detects strongly connected components in the dependency graph.
 * In this project, SCCs with more than one node represent
 * persistence cycles or backdoor reinstallation loops.
 *
 * Used in persistence analysis.
 */
public class TarjanSCC {

    private DependencyGraph dependencyGraph;
    private int[] discoveryTime;
    private int[] lowLinkValue;
    private boolean[] isOnStack;
    private Stack<Integer> dfsStack;
    private int currentTime;
    private List<List<Integer>> stronglyConnectedComponents;
    private int maxNodeId;

    public TarjanSCC(DependencyGraph dependencyGraph, int maxNodeId) {
        this.dependencyGraph = dependencyGraph;
        this.maxNodeId = maxNodeId + 1;

        this.discoveryTime = new int[this.maxNodeId];
        this.lowLinkValue = new int[this.maxNodeId];
        this.isOnStack = new boolean[this.maxNodeId];
        this.dfsStack = new Stack<>();
        this.currentTime = 0;
        this.stronglyConnectedComponents = new ArrayList<>();

        Arrays.fill(discoveryTime, -1);
    }

    /*
     * Runs Tarjan's SCC algorithm on the full dependency graph.
     */
    public List<List<Integer>> findAllSCCs() {
        for (int nodeId : dependencyGraph.getAllNodeIds()) {
            if (discoveryTime[nodeId] == -1) {
                depthFirstSearch(nodeId);
            }
        }
        return stronglyConnectedComponents;
    }

    /*
     * DFS helper for Tarjan's SCC algorithm.
     */
    private void depthFirstSearch(int currentNodeId) {
        discoveryTime[currentNodeId] = lowLinkValue[currentNodeId] = currentTime++;
        dfsStack.push(currentNodeId);
        isOnStack[currentNodeId] = true;

        for (NetworkEdge edge : dependencyGraph.getNeighbors(currentNodeId)) {
            int neighborNodeId = edge.destinationNodeId;

            if (discoveryTime[neighborNodeId] == -1) {
                depthFirstSearch(neighborNodeId);
                lowLinkValue[currentNodeId] = Math.min(
                    lowLinkValue[currentNodeId],
                    lowLinkValue[neighborNodeId]
                );

            } else if (isOnStack[neighborNodeId]) {
                lowLinkValue[currentNodeId] = Math.min(
                    lowLinkValue[currentNodeId],
                    discoveryTime[neighborNodeId]
                );
            }
        }

        // If current node is the root of an SCC
        if (lowLinkValue[currentNodeId] == discoveryTime[currentNodeId]) {
            List<Integer> currentScc = new ArrayList<>();

            while (true) {
                int stackNodeId = dfsStack.pop();
                isOnStack[stackNodeId] = false;
                currentScc.add(stackNodeId);

                if (stackNodeId == currentNodeId) {
                    break;
                }
            }

            stronglyConnectedComponents.add(currentScc);
        }
    }

    /*
     * Returns only SCCs with size > 1,
     * because those represent actual persistence cycles.
     */
    public List<List<Integer>> getPersistenceCycles() {
        List<List<Integer>> persistenceCycles = new ArrayList<>();

        for (List<Integer> scc : stronglyConnectedComponents) {
            if (scc.size() > 1) {
                persistenceCycles.add(scc);
            }
        }

        return persistenceCycles;
    }

    /*
     * Converts persistence cycles into threat events.
     */
    public List<ThreatEvent> getCyclesAsThreatEvents() {
        List<ThreatEvent> threatEvents = new ArrayList<>();

        for (List<Integer> cycleNodeIds : getPersistenceCycles()) {
            ThreatEvent persistenceThreat = new ThreatEvent(
                "PERSISTENCE_CYCLE",
                cycleNodeIds,
                100.0,
                "Backdoor cycle detected: " + cycleNodeIds.size() +
                " components form a mutual reinstallation loop"
            );

            threatEvents.add(persistenceThreat);
        }

        return threatEvents;
    }
}