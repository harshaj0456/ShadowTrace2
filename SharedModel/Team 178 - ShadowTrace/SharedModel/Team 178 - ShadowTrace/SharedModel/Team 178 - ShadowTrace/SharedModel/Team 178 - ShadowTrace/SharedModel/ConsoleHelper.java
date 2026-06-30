package SharedModel;

/*
 * ConsoleHelper centralizes all console formatting used in the project.
 *
 * It improves readability by providing:
 * - boxed headers
 * - clean section dividers
 * - formatted messages (info, success, warning, alert, error)
 * - structured menu display
 * - system instructions
 *
 * This helps make the console output:
 * ✔ readable
 * ✔ professional
 * ✔ demo-friendly for judges
 */
public class ConsoleHelper {

    // ================= MAIN BANNER =================
    public static void printWelcomeBanner() {
        System.out.println("\n╔══════════════════════════════════════════════════════╗");
        System.out.println("║                 SHADOWTRACE SYSTEM                  ║");
        System.out.println("║        Backdoor Persistence & MITM Detection        ║");
        System.out.println("║                 Buffer 7.0 Project                  ║");
        System.out.println("╚══════════════════════════════════════════════════════╝");

        System.out.println("\nℹ️  This is a simulation-based cybersecurity system.");
        System.out.println("   It detects and analyzes:");
        System.out.println("   • MITM (Man-in-the-Middle) attacks");
        System.out.println("   • Persistence / backdoor cycles");
        System.out.println("   • Attack spread across network");
        System.out.println("   • Threat severity & cleanup plan\n");
    }

    // ================= HEADER =================
    public static void printHeader(String title) {
        System.out.println("\n╔══════════════════════════════════════════════════════╗");
        System.out.printf("║ %-52s ║%n", title);
        System.out.println("╚══════════════════════════════════════════════════════╝");
    }

    // ================= SECTION =================
    public static void printSection(String sectionTitle) {
        System.out.println("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("🔷 " + sectionTitle);
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
    }

    // ================= SUB SECTION =================
    public static void printSubSection(String title) {
        System.out.println("\n🔹 " + title);
        System.out.println("----------------------------------------------");
    }

    // ================= MESSAGE TYPES =================
    public static void printInfo(String message) {
        System.out.println("ℹ️  " + message);
    }

    public static void printSuccess(String message) {
        System.out.println("✅ " + message);
    }

    public static void printWarning(String message) {
        System.out.println("⚠️  " + message);
    }

    public static void printAlert(String message) {
        System.out.println("🔥 " + message);
    }

    public static void printError(String message) {
        System.out.println("❌ " + message);
    }

    // ================= MENU =================
    public static void printMainMenu() {
        printSection("MAIN MENU");

        System.out.println("1️⃣  View System Instructions");
        System.out.println("2️⃣  Run Demo 1 - MITM Detection");
        System.out.println("3️⃣  Run Demo 2 - Persistence Detection");
        System.out.println("4️⃣  Run Demo 3 - Full System Analysis");
        System.out.println("5️⃣  View Algorithm Justification");
        System.out.println("6️⃣  Exit");

        System.out.print("\n👉 Enter your choice: ");
    }

    // ================= INSTRUCTIONS =================
    public static void printSystemInstructions() {
        printSection("SYSTEM INSTRUCTIONS");

        System.out.println("1. This project is simulation-based (not real-time monitoring).");
        System.out.println("2. Nodes represent devices, processes, or system components.");
        System.out.println("3. Edges represent communication or dependency relationships.");
        System.out.println("4. Demo 1 → MITM detection using ARP + path deviation.");
        System.out.println("5. Demo 2 → Persistence detection using graph algorithms.");
        System.out.println("6. Demo 3 → Full pipeline including spread + scoring.");
        System.out.println("7. Final report shows threats, impact, and cleanup steps.");
    }

    // ================= ALGORITHM EXPLANATION =================
    public static void printAlgorithmJustification() {
        printSection("ALGORITHM JUSTIFICATION");

        System.out.println("• Dijkstra → Detect abnormal routing (MITM)");
        System.out.println("• HashMap → Fast ARP and graph lookup (O(1))");
        System.out.println("• Tarjan SCC → Detect hidden persistence cycles");
        System.out.println("• Articulation Points → Find critical nodes");
        System.out.println("• Betweenness Centrality → Find entry point");
        System.out.println("• BFS → Simulate attack spread");
        System.out.println("• DSU → Group compromised nodes");
        System.out.println("• Max Heap → Rank threats by severity");
        System.out.println("• Topological Sort → Safe removal order");
        System.out.println("• Bloom Filter → Avoid duplicate operations");
    }

    // ================= REPORT HEADER =================
    public static void printReportHeader() {
        System.out.println("\n╔══════════════════════════════════════════════════════╗");
        System.out.println("║                INCIDENT REPORT                      ║");
        System.out.println("╚══════════════════════════════════════════════════════╝");
    }
}