/**
 * NetworkInfo.java
 *
 * Description:
 *   NetworkInfo is an InfoCollector module that retrieves details
 *   about network interfaces using OSHI's NetworkIF API. It reports
 *   interface name, display name, MAC address, IP addresses, speed,
 *   and traffic statistics (bytes sent/received).
 *
 * Purpose:
 *   - Provide detailed network monitoring for SysLens.
 *   - Support both human-readable output (collect()) and JSON export (toJson()).
 *   - Allow contributors to extend monitoring with additional metrics
 *     such as per-interface packet counts, error rates, or connection status.
 *
 * Notes for Contributors:
 *   - Uses OSHI's SystemInfo().getHardware().getNetworkIFs(true).
 *   - updateAttributes() must be called before reading interface stats.
 *   - Extend by adding new fields or integrating with other networking APIs.
 *   - Use formatBytes() and formatSpeed() helpers for consistent output.
 */

package com.aurexiris.syslens.modules;

import com.aurexiris.syslens.core.InfoCollector;
import oshi.SystemInfo;
import oshi.hardware.NetworkIF;

import java.util.List;

public class NetworkInfo implements InfoCollector {

    // List of detected network interfaces
    private final List<NetworkIF> networks =
            new SystemInfo().getHardware().getNetworkIFs(true);

    @Override
    public String getName() {
        return "Network Info";
    }

    /**
     * Collect network information in a human-readable format.
     * Includes interface name, MAC, IP addresses, speed, and traffic stats.
     * @return formatted string with network details
     */
    @Override
    public String collect() {
        StringBuilder sb = new StringBuilder();
        sb.append("""
                ==============================
                     NETWORK INFORMATION
                ==============================
                """);

        for (NetworkIF net : networks) {
            net.updateAttributes();

            String ipv4 = net.getIPv4addr().length > 0
                    ? net.getIPv4addr()[0] : "N/A";
            String ipv6 = net.getIPv6addr().length > 0
                    ? net.getIPv6addr()[0] : "N/A";

            sb.append("""
                    Interface   : %s
                    Display     : %s
                    MAC         : %s
                    IPv4        : %s
                    IPv6        : %s
                    Speed       : %s
                    Sent        : %s
                    Received    : %s
                    ------------------------------
                    """.formatted(
                    net.getName(),
                    net.getDisplayName(),
                    net.getMacaddr(),
                    ipv4,
                    ipv6,
                    formatSpeed(net.getSpeed()),
                    formatBytes(net.getBytesSent()),
                    formatBytes(net.getBytesRecv())
            ));
        }

        sb.append("==============================");
        return sb.toString();
    }

    /**
     * Export network information as JSON.
     * @return JSON string with network details
     */
    @Override
    public String toJson() {
        StringBuilder sb = new StringBuilder("[\n");

        for (int i = 0; i < networks.size(); i++) {
            NetworkIF net = networks.get(i);
            net.updateAttributes();

            String ipv4 = net.getIPv4addr().length > 0
                    ? net.getIPv4addr()[0] : "N/A";
            String ipv6 = net.getIPv6addr().length > 0
                    ? net.getIPv6addr()[0] : "N/A";

            sb.append("""
                      {
                        "name": "%s",
                        "displayName": "%s",
                        "mac": "%s",
                        "ipv4": "%s",
                        "ipv6": "%s",
                        "speedBps": %d,
                        "bytesSent": %d,
                        "bytesReceived": %d
                      }""".formatted(
                    net.getName(),
                    net.getDisplayName(),
                    net.getMacaddr(),
                    ipv4,
                    ipv6,
                    net.getSpeed(),
                    net.getBytesSent(),
                    net.getBytesRecv()
            ));

            if (i < networks.size() - 1) sb.append(",");
            sb.append("\n");
        }

        sb.append("]");
        return sb.toString();
    }

    // Helper method to format byte values into human-readable strings
    private String formatBytes(long bytes) {
        if (bytes >= 1_073_741_824L)
            return String.format("%.2f GB", bytes / 1_073_741_824.0);
        if (bytes >= 1_048_576L)
            return String.format("%.2f MB", bytes / 1_048_576.0);
        if (bytes >= 1_024L)
            return String.format("%.2f KB", bytes / 1_024.0);
        return bytes + " B";
    }

    // Helper method to format speed values into human-readable strings
    private String formatSpeed(long bps) {
        if (bps >= 1_000_000_000L)
            return String.format("%.1f Gbps", bps / 1_000_000_000.0);
        if (bps >= 1_000_000L)
            return String.format("%.1f Mbps", bps / 1_000_000.0);
        if (bps >= 1_000L)
            return String.format("%.1f Kbps", bps / 1_000.0);
        return bps + " bps";
    }
}
