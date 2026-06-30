package SharedModel;

import java.util.*;

/*
 * ThreatScorer computes a normalized risk score for each detected threat.
 * It combines multiple signals such as:
 * - persistence cycle size
 * - MITM confidence
 * - spread reach
 *
 * Used by RiskHeap for ranking threats.
 */
public class ThreatScorer {

    /*
     * Returns a computed risk score between 0 and 100.
     */
    public double scoreThreat(ThreatEvent threatEvent, Set<Integer> compromisedNodeIds) {
        double totalScore = 0.0;

        // Factor 1: persistence cycle size
        double persistenceCycleScore = 0.0;
        if ("PERSISTENCE_CYCLE".equals(threatEvent.threatType)) {
            persistenceCycleScore = Math.min(threatEvent.affectedNodeIds.size() * 20.0, 40.0);
        }

        // Factor 2: MITM detection confidence
        double mitmConfidenceScore = 0.0;
        if ("MITM".equals(threatEvent.threatType)) {
            mitmConfidenceScore = threatEvent.confidenceScore * 0.30;
        }

        // Factor 3: attack spread reach
        double spreadReachScore = 0.0;
        if ("SPREAD".equals(threatEvent.threatType) || !compromisedNodeIds.isEmpty()) {
            spreadReachScore = Math.min(compromisedNodeIds.size() * 2.0, 30.0);
        }

        totalScore = persistenceCycleScore + mitmConfidenceScore + spreadReachScore;

        return Math.min(totalScore, 100.0);
    }
}