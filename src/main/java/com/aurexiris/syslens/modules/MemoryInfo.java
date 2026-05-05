/**
 * MemoryInfo.java
 *
 * Description:
 *   MemoryInfo is an InfoCollector module that retrieves details
 *   about system memory (RAM and swap) using OSHI's GlobalMemory
 *   and VirtualMemory APIs. It reports total, used, and free memory
 *   values along with usage percentages.
 *
 * Purpose:
 *   - Provide detailed memory monitoring for SysLens.
 *   - Support both human-readable output (collect()) and JSON export (toJson()).
 *   - Allow contributors to extend monitoring with additional memory metrics
 *     such as per-bank details or paging statistics.
 *
 * Notes for Contributors:
 *   - Uses OSHI's SystemInfo().getHardware().getMemory().
 *   - Swap usage is reported only if swap is available.
 *   - Extend by adding new fields or integrating with other memory APIs.
 *   - Use formatBytes() helper for consistent human-readable output.
 */

package com.aurexiris.syslens.modules;

import com.aurexiris.syslens.core.InfoCollector;
import oshi.SystemInfo;
import oshi.hardware.GlobalMemory;
import oshi.hardware.VirtualMemory;

public class MemoryInfo implements InfoCollector {

    // Reference to the system's memory via OSHI
    private final GlobalMemory memory =
            new SystemInfo().getHardware().getMemory();

    @Override
    public String getName() {
        return "Memory Info";
    }

    /**
     * Collect memory information in a human-readable format.
     * Includes total, used, and free RAM, as well as swap usage.
     * @return formatted string with memory details
     */
    @Override
    public String collect() {
        VirtualMemory vm = memory.getVirtualMemory();

        long totalRam     = memory.getTotal();
        long availableRam = memory.getAvailable();
        long usedRam      = totalRam - availableRam;

        long totalSwap    = vm.getSwapTotal();
        long usedSwap     = vm.getSwapUsed();

        double ramUsedPct  = (usedRam  * 100.0) / totalRam;
        double swapUsedPct = totalSwap > 0
                ? (usedSwap * 100.0) / totalSwap
                : 0.0;

        return """
                ==============================
                      MEMORY INFORMATION
                ==============================
                Total RAM   : %s
                Used RAM    : %s (%.1f%%)
                Free RAM    : %s
                ------------------------------
                Total Swap  : %s
                Used Swap   : %s (%.1f%%)
                Free Swap   : %s
                ==============================
                """.formatted(
                formatBytes(totalRam),
                formatBytes(usedRam),   ramUsedPct,
                formatBytes(availableRam),
                formatBytes(totalSwap),
                formatBytes(usedSwap),  swapUsedPct,
                formatBytes(totalSwap - usedSwap)
        );
    }

    /**
     * Export memory information as JSON.
     * @return JSON string with memory details
     */
    @Override
    public String toJson() {
        VirtualMemory vm = memory.getVirtualMemory();

        long totalRam     = memory.getTotal();
        long availableRam = memory.getAvailable();
        long usedRam      = totalRam - availableRam;
        long totalSwap    = vm.getSwapTotal();
        long usedSwap     = vm.getSwapUsed();

        double ramUsedPct  = (usedRam  * 100.0) / totalRam;
        double swapUsedPct = totalSwap > 0
                ? (usedSwap * 100.0) / totalSwap
                : 0.0;

        return """
                {
                  "ram": {
                    "totalBytes": %d,
                    "usedBytes": %d,
                    "freeBytes": %d,
                    "usedPercent": %.1f
                  },
                  "swap": {
                    "totalBytes": %d,
                    "usedBytes": %d,
                    "freeBytes": %d,
                    "usedPercent": %.1f
                  }
                }""".formatted(
                totalRam, usedRam, availableRam, ramUsedPct,
                totalSwap, usedSwap, (totalSwap - usedSwap), swapUsedPct
        );
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
}
