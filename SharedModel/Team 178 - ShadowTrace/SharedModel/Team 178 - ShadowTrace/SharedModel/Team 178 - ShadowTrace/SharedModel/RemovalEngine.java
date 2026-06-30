package SharedModel;

import java.util.*;

/*
 * RemovalEngine generates a safe step-by-step cleanup plan
 * for persistence-related threats.
 *
 * It uses:
 * - TopologicalSorter for dependency-safe node removal order
 * - BloomFilter to avoid duplicate scheduling
 *
 * Used in final incident response planning.
 */
public class RemovalEngine {

    private TopologicalSorter topologicalSorter;
    private BloomFilter scheduledNodeFilter;

    public RemovalEngine() {
        topologicalSorter = new TopologicalSorter();
        scheduledNodeFilter = new BloomFilter(1000);
    }

    /*
     * Builds a safe removal plan for all persistence threats.
     */
    public List<String> computeRemovalPlan(List<ThreatEvent> persistenceThreatEvents,
                                           DependencyGraph dependencyGraph) {
        List<String> removalPlanSteps = new ArrayList<>();

        for (ThreatEvent threatEvent : persistenceThreatEvents) {
            List<Integer> safeRemovalOrder = topologicalSorter.getSafeRemovalOrder(
                threatEvent.affectedNodeIds,
                dependencyGraph
            );

            removalPlanSteps.add("=== Removal plan for persistence cycle: " +
                                 threatEvent.affectedNodeIds + " ===");

            for (int stepIndex = 0; stepIndex < safeRemovalOrder.size(); stepIndex++) {
                int currentNodeId = safeRemovalOrder.get(stepIndex);

                if (scheduledNodeFilter.mightBeFlagged(currentNodeId)) {
                    removalPlanSteps.add("Step " + (stepIndex + 1) +
                                         ": Node " + currentNodeId +
                                         " [already scheduled - skip]");
                } else {
                    removalPlanSteps.add("Step " + (stepIndex + 1) +
                                         ": Remove node " + currentNodeId);
                    scheduledNodeFilter.add(currentNodeId);
                }
            }
        }

        return removalPlanSteps;
    }
}