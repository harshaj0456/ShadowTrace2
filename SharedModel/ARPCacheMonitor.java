package SharedModel;

import java.util.*;

/*
 * ARPCacheMonitor detects suspicious ARP behavior such as:
 * - fake IP to MAC mapping
 * - same MAC claimed by multiple IPs
 *
 * This helps identify ARP spoofing attempts,
 * which are common in MITM attacks.
 *
 * Used by MITMDetector.
 */
public class ARPCacheMonitor {

    // Stores trusted IP -> MAC mappings
    private Map<String, String> legitimateArpTable;

    // Stores observed IP -> MAC mappings
    private Map<String, String> observedArpTable;

    // Stores MAC -> list of IPs claiming that MAC
    private Map<String, List<String>> macAddressToIpList;

    // Stores all ARP-based threat events
    private List<ThreatEvent> detectedThreats;

    public ARPCacheMonitor() {
        legitimateArpTable = new HashMap<>();
        observedArpTable = new HashMap<>();
        macAddressToIpList = new HashMap<>();
        detectedThreats = new ArrayList<>();
    }

    /*
     * Loads a legitimate ARP mapping into the trusted table.
     */
    public void loadLegitimateARP(String ipAddress, String macAddress) {
        legitimateArpTable.put(ipAddress, macAddress);
    }

    /*
     * Observes a new ARP entry and checks for suspicious conditions.
     */
    public void observeARPEntry(String ipAddress, String macAddress, int reportingNodeId) {

        // Check 1: IP exists in trusted table but MAC is different
        if (legitimateArpTable.containsKey(ipAddress) &&
            !legitimateArpTable.get(ipAddress).equals(macAddress)) {

            ThreatEvent spoofingThreat = new ThreatEvent(
                "MITM",
                Arrays.asList(reportingNodeId),
                90.0,
                "ARP spoofing detected: IP " + ipAddress +
                " is claiming MAC " + macAddress +
                ", but the legitimate MAC is " + legitimateArpTable.get(ipAddress)
            );

            detectedThreats.add(spoofingThreat);
            System.out.println("ALERT: " + spoofingThreat.description);
        }

        // Check 2: Same MAC claimed by multiple IPs
        macAddressToIpList.computeIfAbsent(macAddress, key -> new ArrayList<>());
        List<String> claimantIpAddresses = macAddressToIpList.get(macAddress);

        if (!claimantIpAddresses.contains(ipAddress)) {
            claimantIpAddresses.add(ipAddress);
        }

        if (claimantIpAddresses.size() > 1) {
            ThreatEvent duplicateMacThreat = new ThreatEvent(
                "MITM",
                Arrays.asList(reportingNodeId),
                95.0,
                "Duplicate MAC detected: " + macAddress +
                " is claimed by IPs " + claimantIpAddresses
            );

            detectedThreats.add(duplicateMacThreat);
            System.out.println("ALERT: " + duplicateMacThreat.description);
        }

        observedArpTable.put(ipAddress, macAddress);
    }

    /*
     * Returns all detected ARP-related threats.
     */
    public List<ThreatEvent> getDetectedThreats() {
        return detectedThreats;
    }
}