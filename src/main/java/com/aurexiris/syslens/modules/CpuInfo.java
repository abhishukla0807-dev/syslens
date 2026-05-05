/**
 * CpuInfo.java
 *
 * Description:
 *   CpuInfo is an InfoCollector module that retrieves CPU details
 *   using OSHI's CentralProcessor API. It reports processor name,
 *   physical and logical core counts, base frequency, and current load.
 *
 * Purpose:
 *   - Provide detailed CPU monitoring for SysLens.
 *   - Support both human-readable output (collect()) and JSON export (toJson()).
 *   - Allow contributors to extend monitoring with additional CPU metrics
 *     such as per-core load, cache sizes, or temperature.
 *
 * Notes for Contributors:
 *   - Uses OSHI's CentralProcessor to query CPU attributes.
 *   - A short delay (Thread.sleep) is used to calculate load between ticks.
 *   - Extend by adding new fields or integrating with other sensor libraries.
 *   - Ensure consistent formatting for both text and JSON outputs.
 */

package com.aurexiris.syslens.modules;

import com.aurexiris.syslens.core.InfoCollector;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;

public class CpuInfo implements InfoCollector {

    // Reference to the system's CPU via OSHI
    private final CentralProcessor cpu =
            new SystemInfo().getHardware().getProcessor();

    @Override
    public String getName() {
        return "CPU Info";
    }

    /**
     * Collect CPU information in a human-readable format.
     * Includes name, core counts, base frequency, and current load.
     * @return formatted string with CPU details
     */
    @Override
    public String collect() {
        // Capture initial CPU ticks
        long[] prevTicks = cpu.getSystemCpuLoadTicks();

        // Small delay for accurate load calculation
        try { Thread.sleep(1000); } catch (InterruptedException ignored) {}

        double cpuLoad = cpu.getSystemCpuLoadBetweenTicks(prevTicks) * 100;

        return """
                ==============================
                        CPU INFORMATION
                ==============================
                Name        : %s
                Physical    : %d cores
                Logical     : %d threads
                Base Freq   : %.2f GHz
                CPU Load    : %.1f%%
                ==============================
                """.formatted(
                cpu.getProcessorIdentifier().getName(),
                cpu.getPhysicalProcessorCount(),
                cpu.getLogicalProcessorCount(),
                cpu.getProcessorIdentifier().getVendorFreq() / 1_000_000_000.0,
                cpuLoad
        );
    }

    /**
     * Export CPU information as JSON.
     * @return JSON string with CPU details
     */
    @Override
    public String toJson() {
        return """
                {
                  "name": "%s",
                  "physicalCores": %d,
                  "logicalThreads": %d,
                  "baseFreqGHz": %.2f
                }""".formatted(
                cpu.getProcessorIdentifier().getName(),
                cpu.getPhysicalProcessorCount(),
                cpu.getLogicalProcessorCount(),
                cpu.getProcessorIdentifier().getVendorFreq() / 1_000_000_000.0
        );
    }
}
