/**
 * UptimeInfo.java
 *
 * Author: Aman
 * Version: 1.0.0
 * Last Updated: 2026-05-05
 *
 * Description:
 *   UptimeInfo is an InfoCollector module that retrieves system uptime
 *   and boot time using OSHI's OperatingSystem API. It reports how long
 *   the system has been running since the last boot, both in seconds and
 *   in a human-readable format.
 *
 * Purpose:
 *   - Provide visibility into system uptime for SysLens monitoring.
 *   - Support both human-readable output (collect()) and JSON export (toJson()).
 *   - Allow contributors to extend monitoring with additional time-related
 *     metrics such as idle time or session duration.
 *
 * Notes for Contributors:
 *   - Uses OSHI's OperatingSystem.getSystemUptime() and getSystemBootTime().
 *   - Boot time is formatted using the system default timezone.
 *   - Extend by adding new fields or integrating with other OSHI APIs.
 *   - Use formatUptime() helper for consistent human-readable output.
 */

package com.aurexiris.syslens.modules;

import com.aurexiris.syslens.core.InfoCollector;
import oshi.SystemInfo;
import oshi.software.os.OperatingSystem;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class UptimeInfo implements InfoCollector {

    // Reference to the operating system via OSHI
    private final OperatingSystem os =
            new SystemInfo().getOperatingSystem();

    @Override
    public String getName() {
        return "Uptime Info";
    }

    /**
     * Collect uptime information in a human-readable format.
     * Includes boot time and formatted uptime duration.
     * @return formatted string with uptime details
     */
    @Override
    public String collect() {
        long uptimeSeconds = os.getSystemUptime();
        long bootTimeEpoch = os.getSystemBootTime();

        String bootTime = Instant.ofEpochSecond(bootTimeEpoch)
                .atZone(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));

        return """
                ==============================
                      UPTIME INFORMATION
                ==============================
                Boot Time   : %s
                Uptime      : %s
                ==============================
                """.formatted(
                bootTime,
                formatUptime(uptimeSeconds)
        );
    }

    /**
     * Export uptime information as JSON.
     * @return JSON string with uptime details
     */
    @Override
    public String toJson() {
        long uptimeSeconds = os.getSystemUptime();
        long bootTimeEpoch = os.getSystemBootTime();

        String bootTime = Instant.ofEpochSecond(bootTimeEpoch)
                .atZone(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));

        return """
                {
                  "bootTime": "%s",
                  "uptimeSeconds": %d,
                  "uptimeFormatted": "%s"
                }""".formatted(
                bootTime,
                uptimeSeconds,
                formatUptime(uptimeSeconds)
        );
    }

    // Helper method to format uptime into human-readable strings
    private String formatUptime(long seconds) {
        long days    = seconds / 86400;
        long hours   = (seconds % 86400) / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs    = seconds % 60;

        if (days > 0)
            return String.format("%dd %dh %02dm %02ds", days, hours, minutes, secs);
        if (hours > 0)
            return String.format("%dh %02dm %02ds", hours, minutes, secs);
        return String.format("%dm %02ds", minutes, secs);
    }
}
