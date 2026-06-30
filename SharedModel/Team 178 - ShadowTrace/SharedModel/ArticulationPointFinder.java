package SharedModel;

import java.util.*;

/*
 * ArticulationPointFinder identifies critical nodes in the dependency graph.
 * These are nodes whose removal significantly affects graph connectivity.
 *
 * Used in persistence analysis to find structurally important components.
 */
public class ArticulationPointFinder {

    private DependencyGraph dependencyGraph;
    private int[] discoveryTime;
    private int[] lowLinkValue;
    private int[] parentNode;
    private boolean[] visited;
    private Set<Integer> articulationPointSet;
    private int currentTime;
    private int maxNodeId;

    public ArticulationPointFinder(DependencyGraph dependencyGraph, int maxNodeId) {
        this.dependencyGraph = dependencyGraph;
        this.maxNodeId = maxNodeId + 1;

        this.discoveryTime = new int[this.maxNodeId];
        this.lowLinkValue = new int[this.maxNodeId];
        this.parentNode = new int[this.maxNodeId];
        this.visited = new boolean[this.maxNodeId];
        this.articulationPointSet = new HashSet<>();
        this.currentTime = 0;

        Arrays.fill(parentNode, -1);
    }

    /*
     * Finds all articulation points in the dependency graph.
     */
    public Set<Integer> findArticulationPoints() {
        for (int nodeId : dependencyGraph.getAllNodeIds()) {
            if (!visited[nodeId]) {
                depthFirstSearch(nodeId);
            }
        }

        return articulationPointSet;
    }

    /*
     * DFS helper for articulation point detection.
     */
    private void depthFirstSearch(int currentNodeId) {
        visited[currentNodeId] = true;
        discoveryTime[currentNodeId] = lowLinkValue[currentNodeId] = currentTime++;
        int childCount = 0;

        for (NetworkEdge edge : dependencyGraph.getNeighbors(currentNodeId)) {
            int neighborNodeId = edge.destinationNodeId;

            if (!visited[neighborNodeId]) {
                childCount++;
                parentNode[neighborNodeId] = currentNodeId;

                depthFirstSearch(neighborNodeId);

                lowLinkValue[currentNodeId] = Math.min(
                    lowLinkValue[currentNodeId],
                    lowLinkValue[neighborNodeId]
                );

                // Case 1: current node is root and has more than one child
                if (parentNode[currentNodeId] == -1 && childCount > 1) {
                    articulationPointSet.add(currentNodeId);
                }

                // Case 2: current node is not root, and no back edge exists
                if (parentNode[currentNodeId] != -1 &&
                    lowLinkValue[neighborNodeId] >= discoveryTime[currentNodeId]) {
                    articulationPointSet.add(currentNodeId);
                }

            } else if (neighborNodeId != parentNode[currentNodeId]) {
                lowLinkValue[currentNodeId] = Math.min(
                    lowLinkValue[currentNodeId],
                    discoveryTime[neighborNodeId]
                );
            }
        }
    }
}