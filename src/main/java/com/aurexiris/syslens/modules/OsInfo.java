/**
 * OsInfo.java
 *
 * Author: Aman
 * Version: 1.0.0
 * Last Updated: 2026-05-05
 *
 * Description:
 *   OsInfo is an InfoCollector module that retrieves details about
 *   the operating system using OSHI's OperatingSystem API. It reports
 *   OS family, version, architecture, manufacturer, and process count.
 *
 * Purpose:
 *   - Provide detailed operating system information for SysLens.
 *   - Support both human-readable output (collect()) and JSON export (toJson()).
 *   - Allow contributors to extend monitoring with additional OS metrics
 *     such as uptime, kernel version, or system boot time.
 *
 * Notes for Contributors:
 *   - Uses OSHI's SystemInfo().getOperatingSystem().
 *   - Extend by adding new fields or integrating with other OS APIs.
 *   - Ensure consistent formatting for both text and JSON outputs.
 */

package com.aurexiris.syslens.modules;

import com.aurexiris.syslens.core.InfoCollector;
import oshi.SystemInfo;
import oshi.software.os.OperatingSystem;

public class OsInfo implements InfoCollector {

    // Reference to the operating system via OSHI
    private final SystemInfo si = new SystemInfo();
    private final OperatingSystem os = si.getOperatingSystem();

    @Override
    public String getName() {
        return "OS Info";
    }

    /**
     * Collect operating system information in a human-readable format.
     * Includes OS family, version, architecture, manufacturer, and process count.
     * @return formatted string with OS details
     */
    @Override
    public String collect() {
        return """
                ==============================
                        OS INFORMATION
                ==============================
                OS          : %s
                Version     : %s
                Arch        : %s
                Manufacturer: %s
                Processes   : %d
                ==============================
                """.formatted(
                os.getFamily(),
                os.getVersionInfo().getVersion(),
                System.getProperty("os.arch"),
                os.getManufacturer(),
                os.getProcessCount()
        );
    }

    /**
     * Export operating system information as JSON.
     * @return JSON string with OS details
     */
    @Override
    public String toJson() {
        return """
                {
                  "family": "%s",
                  "version": "%s",
                  "arch": "%s",
                  "manufacturer": "%s",
                  "processCount": %d
                }""".formatted(
                os.getFamily(),
                os.getVersionInfo().getVersion(),
                System.getProperty("os.arch"),
                os.getManufacturer(),
                os.getProcessCount()
        );
    }
}
