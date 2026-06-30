package SharedModel;

import java.util.*;

/*
 * Main is the console entry point of the ShadowTrace system.
 *
 * Responsibilities:
 * - show welcome banner
 * - display system instructions
 * - run ready-made demo scenarios
 * - validate user menu input
 * - connect all major modules together
 *
 * This class does not implement algorithms directly.
 * It coordinates:
 * - MITM detection
 * - Persistence detection
 * - Spread simulation
 * - Threat scoring
 * - Safe removal planning
 * - Incident reporting
 */
public class Main {

    // Shared scanner for reading console input
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        ConsoleHelper.printWelcomeBanner();
        runMainMenu();
    }

    /*
     * Runs the main menu until the user chooses to exit.
     */
    private static void runMainMenu() {
        int userChoice;

        do {
            ConsoleHelper.printMainMenu();
            userChoice = readMenuChoice(1, 6);

            switch (userChoice) {
                case 1:
                    ConsoleHelper.printSystemInstructions();
                    break;

                case 2:
                    runMitmDemo();
                    break;

                case 3:
                    runPersistenceDemo();
                    break;

                case 4:
                    runFullIntegratedDemo();
                    break;

                case 5:
                    ConsoleHelper.printAlgorithmJustification();
                    break;

                case 6:
                    ConsoleHelper.printSuccess("Exiting ShadowTrace. Thank you.");
                    break;

                default:
                    ConsoleHelper.printError("Invalid choice. Please try again.");
            }

        } while (userChoice != 6);
    }

    /*
     * Reads and validates menu choice using InputValidator.
     */
    private static int readMenuChoice(int minimumChoice, int maximumChoice) {
        while (true) {
            try {
                int chosenValue = Integer.parseInt(scanner.nextLine().trim());

                if (!InputValidator.isValidMenuChoice(chosenValue, minimumChoice, maximumChoice)) {
                    ConsoleHelper.printWarning(
                        "Please enter a valid option between " + minimumChoice + " and " + maximumChoice + "."
                    );
                    System.out.print("👉 Enter your choice: ");
                    continue;
                }

                return chosenValue;

            } catch (NumberFormatException exception) {
                ConsoleHelper.printError("Invalid input. Please enter a numeric menu choice.");
                System.out.print("👉 Enter your choice: ");
            }
        }
    }

    /*
     * Demo 1:
     * Demonstrates MITM detection using:
     * - ARP spoofing detection
     * - path deviation analysis
     */
    private static void runMitmDemo() {
        ConsoleHelper.printSection("DEMO 1 - MITM DETECTION");
        ConsoleHelper.printInfo("Building communication graph...");

        NetworkGraph networkGraph = new NetworkGraph();

        networkGraph.addNode(new NetworkNode(1, "NodeA", "device", 0.9));
        networkGraph.addNode(new NetworkNode(2, "Router", "device", 1.0));
        networkGraph.addNode(new NetworkNode(3, "NodeB", "device", 0.8));
        networkGraph.addNode(new NetworkNode(4, "Attacker", "device", 0.1));

        networkGraph.addEdge(new NetworkEdge(1, 2, 0.9, "network"));
        networkGraph.addEdge(new NetworkEdge(2, 3, 0.8, "network"));
        networkGraph.addEdge(new NetworkEdge(1, 4, 0.6, "network"));
        networkGraph.addEdge(new NetworkEdge(4, 3, 0.6, "network"));

        ConsoleHelper.printSuccess("Communication graph initialized successfully.");

        MITMDetector mitmDetector = new MITMDetector(networkGraph);

        // Legitimate ARP table entries
        String[][] legitimateArpEntries = {
            {"192.168.1.1", "AA:BB:CC:DD:EE:01"},
            {"192.168.1.2", "AA:BB:CC:DD:EE:02"},
            {"192.168.1.3", "AA:BB:CC:DD:EE:03"}
        };

        for (String[] arpEntry : legitimateArpEntries) {
            if (InputValidator.isValidIPv4(arpEntry[0]) &&
                InputValidator.isValidMacAddress(arpEntry[1])) {
                mitmDetector.loadLegitimateARP(arpEntry[0], arpEntry[1]);
            }
        }

        ConsoleHelper.printInfo("Running ARP monitoring checks...");

        // Observed ARP entries (includes spoofing)
        String[][] observedArpEntries = {
            {"192.168.1.1", "AA:BB:CC:DD:EE:01", "1"},
            {"192.168.1.3", "AA:BB:CC:DD:EE:03", "3"},
            {"192.168.1.2", "AA:BB:CC:DD:EE:01", "4"}
        };

        for (String[] observedEntry : observedArpEntries) {
            String ipAddress = observedEntry[0];
            String macAddress = observedEntry[1];
            int reportingNodeId = Integer.parseInt(observedEntry[2]);

            if (InputValidator.isValidIPv4(ipAddress) &&
                InputValidator.isValidMacAddress(macAddress) &&
                InputValidator.isValidNodeId(reportingNodeId)) {

                mitmDetector.loadARPEntry(ipAddress, macAddress, reportingNodeId);
            }
        }

        ConsoleHelper.printInfo("Running path deviation analysis...");
        List<Integer> actualObservedPath = Arrays.asList(1, 4, 3);
        mitmDetector.analyzePathDeviation(1, 3, actualObservedPath);

        List<ThreatEvent> detectedMitmThreats = mitmDetector.getAllThreats();

        ConsoleHelper.printSubSection("MITM THREAT SUMMARY");
        ConsoleHelper.printSuccess("MITM threats detected: " + detectedMitmThreats.size());

        for (ThreatEvent threatEvent : detectedMitmThreats) {
            System.out.println("🔸 Threat Type : " + threatEvent.threatType);
            System.out.println("   Score       : " + threatEvent.confidenceScore);
            System.out.println("   Description : " + threatEvent.description);
            System.out.println();
        }
    }

    /*
     * Demo 2:
     * Demonstrates persistence analysis using:
     * - Tarjan SCC
     * - articulation point detection
     * - betweenness centrality
     */
    private static void runPersistenceDemo() {
        ConsoleHelper.printSection("DEMO 2 - PERSISTENCE DETECTION");
        ConsoleHelper.printInfo("Building dependency graph...");

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

        ConsoleHelper.printSuccess("Dependency graph initialized successfully.");

        ConsoleHelper.printInfo("Running Tarjan SCC...");
        TarjanSCC tarjanScc = new TarjanSCC(dependencyGraph, 5);
        tarjanScc.findAllSCCs();
        List<ThreatEvent> persistenceThreatEvents = tarjanScc.getCyclesAsThreatEvents();

        ConsoleHelper.printInfo("Running articulation point detection...");
        ArticulationPointFinder articulationPointFinder =
            new ArticulationPointFinder(dependencyGraph, 5);
        Set<Integer> articulationPoints =
            articulationPointFinder.findArticulationPoints();

        ConsoleHelper.printInfo("Running betweenness centrality...");
        BetweennessCentrality betweennessCentrality =
            new BetweennessCentrality(dependencyGraph);
        int likelyEntryPointNodeId =
            betweennessCentrality.findEntryPoint();

        ConsoleHelper.printSubSection("PERSISTENCE THREAT SUMMARY");
        ConsoleHelper.printSuccess("Persistence cycles found: " + persistenceThreatEvents.size());

        for (ThreatEvent threatEvent : persistenceThreatEvents) {
            System.out.println("🔸 Threat Type : " + threatEvent.threatType);
            System.out.println("   Nodes       : " + threatEvent.affectedNodeIds);
            System.out.println("   Description : " + threatEvent.description);
            System.out.println();
        }

        System.out.println("Critical articulation nodes : " + articulationPoints);
        System.out.println("Likely entry point node     : " + likelyEntryPointNodeId);
    }

    /*
     * Demo 3:
     * Runs the full integrated pipeline:
     * - MITM detection
     * - Persistence analysis
     * - Spread simulation
     * - Threat scoring
     * - Safe removal planning
     * - Incident report generation
     */
    private static void runFullIntegratedDemo() {
        ConsoleHelper.printSection("DEMO 3 - FULL INTEGRATED ANALYSIS");

        // ================= STEP 1: MITM =================
        ConsoleHelper.printSubSection("STEP 1 - MITM DETECTION");

        NetworkGraph networkGraph = new NetworkGraph();
        networkGraph.addNode(new NetworkNode(1, "NodeA", "device", 0.9));
        networkGraph.addNode(new NetworkNode(2, "Router", "device", 1.0));
        networkGraph.addNode(new NetworkNode(3, "NodeB", "device", 0.8));
        networkGraph.addNode(new NetworkNode(4, "Attacker", "device", 0.1));

        networkGraph.addEdge(new NetworkEdge(1, 2, 0.9, "network"));
        networkGraph.addEdge(new NetworkEdge(2, 3, 0.8, "network"));
        networkGraph.addEdge(new NetworkEdge(1, 4, 0.6, "network"));
        networkGraph.addEdge(new NetworkEdge(4, 3, 0.6, "network"));

        MITMDetector mitmDetector = new MITMDetector(networkGraph);
        mitmDetector.loadLegitimateARP("192.168.1.1", "AA:BB:CC:DD:EE:01");
        mitmDetector.loadLegitimateARP("192.168.1.2", "AA:BB:CC:DD:EE:02");
        mitmDetector.loadLegitimateARP("192.168.1.3", "AA:BB:CC:DD:EE:03");

        mitmDetector.loadARPEntry("192.168.1.1", "AA:BB:CC:DD:EE:01", 1);
        mitmDetector.loadARPEntry("192.168.1.3", "AA:BB:CC:DD:EE:03", 3);
        mitmDetector.loadARPEntry("192.168.1.2", "AA:BB:CC:DD:EE:01", 4);

        List<Integer> actualObservedPath = Arrays.asList(1, 4, 3);
        mitmDetector.analyzePathDeviation(1, 3, actualObservedPath);
        List<ThreatEvent> mitmThreatEvents = mitmDetector.getAllThreats();

        ConsoleHelper.printSuccess("MITM analysis completed.");

        // ================= STEP 2: PERSISTENCE =================
        ConsoleHelper.printSubSection("STEP 2 - PERSISTENCE ANALYSIS");

        DependencyGraph dependencyGraph = new DependencyGraph();

        dependencyGraph.addNode(new NetworkNode(1, "ScheduledTask_A", "task", 0.0));
        dependencyGraph.addNode(new NetworkNode(2, "RegistryKey_B", "registry", 0.0));
        dependencyGraph.addNode(new NetworkNode(3, "RogueProcess_C", "process", 0.0));
        dependencyGraph.addNode(new NetworkNode(4, "RogueAccount_D", "account", 0.0));
        dependencyGraph.addNode(new NetworkNode(5, "StartupScript_E", "task", 0.0));

        dependencyGraph.addDirectedEdge(new NetworkEdge(1, 2, 1.0, "reinstalls"));
        dependencyGraph.addDirectedEdge(new NetworkEdge(2, 3, 1.0, "reinstalls"));
        dependencyGraph.addDirectedEdge(new NetworkEdge(3, 1, 0.3, "reinstalls"));
        dependencyGraph.addDirectedEdge(new NetworkEdge(4, 1, 1.0, "depends_on"));
        dependencyGraph.addDirectedEdge(new NetworkEdge(5, 2, 1.0, "depends_on"));

        TarjanSCC tarjanScc = new TarjanSCC(dependencyGraph, 5);
        tarjanScc.findAllSCCs();
        List<ThreatEvent> persistenceThreatEvents = tarjanScc.getCyclesAsThreatEvents();

        ArticulationPointFinder articulationPointFinder =
            new ArticulationPointFinder(dependencyGraph, 5);
        Set<Integer> articulationPoints =
            articulationPointFinder.findArticulationPoints();

        BetweennessCentrality betweennessCentrality =
            new BetweennessCentrality(dependencyGraph);
        int likelyEntryPointNodeId =
            betweennessCentrality.findEntryPoint();

        ConsoleHelper.printSuccess("Persistence analysis completed.");

        // ================= STEP 3: SPREAD =================
        ConsoleHelper.printSubSection("STEP 3 - SPREAD SIMULATION");

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

        SpreadSimulator spreadSimulator = new SpreadSimulator(networkTopology);
        spreadSimulator.simulate(likelyEntryPointNodeId);
        ThreatEvent spreadThreatEvent = spreadSimulator.getSpreadThreatEvent();

        ConsoleHelper.printSuccess("Spread simulation completed.");

        // ================= STEP 4: SCORING =================
        ConsoleHelper.printSubSection("STEP 4 - THREAT SCORING");

        List<ThreatEvent> allThreatEvents = new ArrayList<>();
        allThreatEvents.addAll(mitmThreatEvents);
        allThreatEvents.addAll(persistenceThreatEvents);
        allThreatEvents.add(spreadThreatEvent);

        RiskHeap riskHeap = new RiskHeap();
        for (ThreatEvent threatEvent : allThreatEvents) {
            riskHeap.addThreat(threatEvent, spreadSimulator.compromisedNodeIds);
        }

        List<ThreatEvent> rankedThreatEvents = riskHeap.getOrderedThreats();
        ThreatEvent highestRiskThreat = riskHeap.getHighestRiskThreat();

        ConsoleHelper.printSuccess("Threat scoring completed.");

        // ================= STEP 5: REMOVAL PLAN =================
        ConsoleHelper.printSubSection("STEP 5 - SAFE REMOVAL PLAN");

        RemovalEngine removalEngine = new RemovalEngine();
        List<String> removalPlanSteps =
            removalEngine.computeRemovalPlan(persistenceThreatEvents, dependencyGraph);

        ConsoleHelper.printSuccess("Safe removal plan generated.");

        // ================= STEP 6: REPORT =================
        ConsoleHelper.printSubSection("STEP 6 - INCIDENT REPORT");

        IncidentReport incidentReport = new IncidentReport(
            rankedThreatEvents,
            removalPlanSteps,
            spreadSimulator.compromisedNodeIds,
            spreadSimulator.cleanNodeIds,
            spreadSimulator.infectionTimeline,
            spreadSimulator.fastestSpreadPath
        );

        ConsoleHelper.printInfo("Critical articulation nodes : " + articulationPoints);
        ConsoleHelper.printInfo("Highest risk threat         : " +
            (highestRiskThreat != null ? highestRiskThreat.threatType : "None"));
        ConsoleHelper.printInfo("Compromised node count      : " +
            spreadSimulator.compromisedNodeIds.size());
        ConsoleHelper.printInfo("Clean node count            : " +
            spreadSimulator.cleanNodeIds.size());
        ConsoleHelper.printInfo("Removal steps computed      : " +
            removalPlanSteps.size());

        ConsoleHelper.printSuccess("Generating final incident report...");
        incidentReport.printToConsole();
        incidentReport.saveToFile("ShadowTrace_Report.txt");
    }
}