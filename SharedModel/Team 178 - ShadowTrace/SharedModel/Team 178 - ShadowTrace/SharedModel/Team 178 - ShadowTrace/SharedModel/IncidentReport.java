package SharedModel;

import java.util.*;
import java.io.*;
import java.time.*;
import java.time.format.*;

/*
 * IncidentReport generates the final incident summary.
 * It can:
 * - print the report to console
 * - save the report to a text file
 *
 * Used after detection, spread simulation, scoring, and removal planning.
 */
public class IncidentReport {

    private List<ThreatEvent> allThreatEvents;
    private List<String> removalPlanSteps;
    private Set<Integer> compromisedNodeIds;
    private Set<Integer> cleanNodeIds;
    private Map<Integer, Integer> infectionTimeline;
    private List<Integer> fastestAttackPath;

    public IncidentReport(List<ThreatEvent> allThreatEvents,
                          List<String> removalPlanSteps,
                          Set<Integer> compromisedNodeIds,
                          Set<Integer> cleanNodeIds,
                          Map<Integer, Integer> infectionTimeline,
                          List<Integer> fastestAttackPath) {
        this.allThreatEvents = allThreatEvents;
        this.removalPlanSteps = removalPlanSteps;
        this.compromisedNodeIds = compromisedNodeIds;
        this.cleanNodeIds = cleanNodeIds;
        this.infectionTimeline = infectionTimeline;
        this.fastestAttackPath = fastestAttackPath;
    }

    /*
     * Prints the full incident report to console.
     */
    public void printToConsole() {
        String separatorLine = "=".repeat(60);
        String generatedTime = LocalDateTime.now()
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        System.out.println(separatorLine);
        System.out.println("         SHADOWTRACE INCIDENT REPORT");
        System.out.println("         Generated: " + generatedTime);
        System.out.println(separatorLine);

        System.out.println("\n[1] THREAT SUMMARY");
        System.out.println("-".repeat(40));
        System.out.println("Total threats detected : " + allThreatEvents.size());
        System.out.println("Compromised nodes      : " + compromisedNodeIds.size());
        System.out.println("Clean nodes            : " + cleanNodeIds.size());
        System.out.println("Fastest attack path    : " + fastestAttackPath);

        System.out.println("\n[2] THREAT DETAILS");
        System.out.println("-".repeat(40));
        for (int index = 0; index < allThreatEvents.size(); index++) {
            ThreatEvent threatEvent = allThreatEvents.get(index);

            System.out.println("Threat #" + (index + 1));
            System.out.println("  Type        : " + threatEvent.threatType);
            System.out.println("  Score       : " + String.format("%.1f", threatEvent.confidenceScore));
            System.out.println("  Nodes       : " + threatEvent.affectedNodeIds);
            System.out.println("  Description : " + threatEvent.description);
            System.out.println();
        }

        System.out.println("[3] INFECTION TIMELINE");
        System.out.println("-".repeat(40));
        infectionTimeline.entrySet()
            .stream()
            .sorted(Map.Entry.comparingByValue())
            .forEach(entry -> System.out.println(
                "  T=" + entry.getValue() + " -> Node " + entry.getKey() + " infected"
            ));

        System.out.println("\n[4] SAFE REMOVAL PLAN");
        System.out.println("-".repeat(40));
        for (String step : removalPlanSteps) {
            System.out.println("  " + step);
        }

        System.out.println("\n[5] CLEAN NODES");
        System.out.println("-".repeat(40));
        System.out.println("  " + cleanNodeIds);

        System.out.println("\n" + separatorLine);
        System.out.println("  END OF REPORT - ShadowTrace");
        System.out.println(separatorLine + "\n");
    }

    /*
     * Saves the report to a text file.
     */
    public void saveToFile(String fileName) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(fileName))) {

            String generatedTime = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            writer.println("SHADOWTRACE INCIDENT REPORT");
            writer.println("Generated: " + generatedTime);
            writer.println("=".repeat(60));

            writer.println("\nTHREAT SUMMARY");
            writer.println("Total threats    : " + allThreatEvents.size());
            writer.println("Compromised nodes: " + compromisedNodeIds.size());
            writer.println("Clean nodes      : " + cleanNodeIds.size());
            writer.println("Fastest path     : " + fastestAttackPath);

            writer.println("\nTHREAT DETAILS");
            for (ThreatEvent threatEvent : allThreatEvents) {
                writer.println("Type: " + threatEvent.threatType +
                               " | Score: " + threatEvent.confidenceScore +
                               " | Nodes: " + threatEvent.affectedNodeIds +
                               " | " + threatEvent.description);
            }

            writer.println("\nINFECTION TIMELINE");
            infectionTimeline.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue())
                .forEach(entry -> writer.println(
                    "T=" + entry.getValue() + " Node " + entry.getKey()
                ));

            writer.println("\nSAFE REMOVAL PLAN");
            for (String step : removalPlanSteps) {
                writer.println(step);
            }

            writer.println("\nCLEAN NODES: " + cleanNodeIds);

            System.out.println("Report saved to: " + fileName);

        } catch (IOException exception) {
            System.out.println("Could not save report: " + exception.getMessage());
        }
    }
}