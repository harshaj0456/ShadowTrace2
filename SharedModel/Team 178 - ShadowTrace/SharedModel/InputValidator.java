package SharedModel;

import java.util.regex.Pattern;

/*
 * InputValidator centralizes input validation logic for the console system.
 *
 * This is important because the project is security-related and should reject:
 * - invalid IDs
 * - empty names
 * - invalid trust scores
 * - malformed IP addresses
 * - malformed MAC addresses
 *
 * Used by Main.java for safe and precise input handling.
 */
public class InputValidator {

    // Strict IPv4 pattern: 0.0.0.0 to 255.255.255.255
    private static final Pattern IPV4_PATTERN = Pattern.compile(
        "^(25[0-5]|2[0-4][0-9]|1[0-9]{2}|[1-9]?[0-9])\\." +
        "(25[0-5]|2[0-4][0-9]|1[0-9]{2}|[1-9]?[0-9])\\." +
        "(25[0-5]|2[0-4][0-9]|1[0-9]{2}|[1-9]?[0-9])\\." +
        "(25[0-5]|2[0-4][0-9]|1[0-9]{2}|[1-9]?[0-9])$"
    );

    // Strict MAC pattern: AA:BB:CC:DD:EE:FF
    private static final Pattern MAC_PATTERN = Pattern.compile(
        "^[0-9A-Fa-f]{2}(:[0-9A-Fa-f]{2}){5}$"
    );

    private InputValidator() {
        // Utility class, no object needed
    }

    /*
     * Validates a menu choice range.
     */
    public static boolean isValidMenuChoice(int choice, int minimum, int maximum) {
        return choice >= minimum && choice <= maximum;
    }

    /*
     * Validates positive node ID.
     */
    public static boolean isValidNodeId(int nodeId) {
        return nodeId > 0;
    }

    /*
     * Validates non-empty node or label name.
     */
    public static boolean isValidName(String value) {
        return value != null && !value.trim().isEmpty();
    }

    /*
     * Validates supported node type.
     */
    public static boolean isValidNodeType(String nodeType) {
        if (nodeType == null) {
            return false;
        }

        String normalizedType = nodeType.trim().toLowerCase();

        return normalizedType.equals("device")
            || normalizedType.equals("process")
            || normalizedType.equals("registry")
            || normalizedType.equals("task")
            || normalizedType.equals("account");
    }

    /*
     * Validates trust score in range [0.0, 1.0].
     */
    public static boolean isValidTrustScore(double trustScore) {
        return trustScore >= 0.0 && trustScore <= 1.0;
    }

    /*
     * Validates edge weight in range [0.0, 1.0].
     */
    public static boolean isValidEdgeWeight(double weight) {
        return weight >= 0.0 && weight <= 1.0;
    }

    /*
     * Validates supported edge type.
     */
    public static boolean isValidEdgeType(String edgeType) {
        if (edgeType == null) {
            return false;
        }

        String normalizedType = edgeType.trim().toLowerCase();

        return normalizedType.equals("network")
            || normalizedType.equals("dependency")
            || normalizedType.equals("reinstalls")
            || normalizedType.equals("depends_on");
    }

    /*
     * Validates IPv4 address format.
     */
    public static boolean isValidIPv4(String ipAddress) {
        return ipAddress != null && IPV4_PATTERN.matcher(ipAddress.trim()).matches();
    }

    /*
     * Validates MAC address format.
     * Example valid: AA:BB:CC:DD:EE:FF
     */
    public static boolean isValidMacAddress(String macAddress) {
        return macAddress != null && MAC_PATTERN.matcher(macAddress.trim()).matches();
    }

    /*
     * Validates a non-empty description or text value.
     */
    public static boolean isValidText(String text) {
        return text != null && !text.trim().isEmpty();
    }
}
