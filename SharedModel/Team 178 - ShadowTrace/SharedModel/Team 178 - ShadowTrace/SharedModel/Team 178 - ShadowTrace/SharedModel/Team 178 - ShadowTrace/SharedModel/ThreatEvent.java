package SharedModel;

import java.util.List;

/*
 * ThreatEvent stores the result of a detected threat.
 *
 * It is used as a common output format across modules such as:
 * - MITMDetector
 * - TarjanSCC
 * - SpreadSimulator
 * - RiskHeap
 * - IncidentReport
 */
public class ThreatEvent {

    // Type of threat: MITM / PERSISTENCE_CYCLE / SPREAD
    public String threatType;

    // IDs of nodes involved in the threat
    public List<Integer> affectedNodeIds;

    // Confidence or severity score assigned to the threat
    public double confidenceScore;

    // Human-readable explanation of the threat
    public String description;

    /*
     * Creates a new threat event.
     */
    public ThreatEvent(String threatType, List<Integer> affectedNodeIds,
                       double confidenceScore, String description) {
        this.threatType = threatType;
        this.affectedNodeIds = affectedNodeIds;
        this.confidenceScore = confidenceScore;
        this.description = description;
    }
}