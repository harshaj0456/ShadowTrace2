package SharedModel;

import java.util.*;

/*
 * RiskHeap ranks detected threats from highest risk to lowest risk.
 * It uses a max-heap so the most dangerous threat is always available first.
 *
 * Used after threat scoring.
 */
public class RiskHeap {

    /*
     * Stores scored threat and its computed risk score.
     */
    private static class ThreatPriorityEntry {
        ThreatEvent threatEvent;
        double riskScore;

        ThreatPriorityEntry(ThreatEvent threatEvent, double riskScore) {
            this.threatEvent = threatEvent;
            this.riskScore = riskScore;
        }
    }

    // Max-heap based on computed risk score
    private PriorityQueue<ThreatPriorityEntry> threatMaxHeap;

    private ThreatScorer threatScorer;

    public RiskHeap() {
        threatMaxHeap = new PriorityQueue<>(
            (first, second) -> Double.compare(second.riskScore, first.riskScore)
        );
        threatScorer = new ThreatScorer();
    }

    /*
     * Computes risk score and inserts threat into max-heap.
     */
    public void addThreat(ThreatEvent threatEvent, Set<Integer> compromisedNodeIds) {
        double computedRiskScore = threatScorer.scoreThreat(threatEvent, compromisedNodeIds);
        threatMaxHeap.offer(new ThreatPriorityEntry(threatEvent, computedRiskScore));

        System.out.println("Added threat: " + threatEvent.threatType +
                           " | computed risk score = " + computedRiskScore);
    }

    /*
     * Returns all threats ordered from highest risk to lowest risk.
     */
    public List<ThreatEvent> getOrderedThreats() {
        List<ThreatEvent> orderedThreatEvents = new ArrayList<>();
        PriorityQueue<ThreatPriorityEntry> heapCopy = new PriorityQueue<>(threatMaxHeap);

        while (!heapCopy.isEmpty()) {
            orderedThreatEvents.add(heapCopy.poll().threatEvent);
        }

        return orderedThreatEvents;
    }

    /*
     * Returns the single highest risk threat.
     */
    public ThreatEvent getHighestRiskThreat() {
        if (threatMaxHeap.isEmpty()) {
            return null;
        }

        return threatMaxHeap.peek().threatEvent;
    }
}