
/**
 * MemoryInfo
 * Module to collect RAM statistics including total, used,
 * free memory and usage percentage.
 */



package com.aurexiris.syslens.modules;

import com.aurexiris.syslens.core.InfoCollector;
import oshi.SystemInfo;
import oshi.hardware.GlobalMemory;
import oshi.hardware.VirtualMemory;

public class MemoryInfo implements InfoCollector {

    private final GlobalMemory memory =
            new SystemInfo().getHardware().getMemory();


    // This method returns the name of this collector, which is used as a key in the snapshot.
    @Override
    public String getName() {
        return "Memory Info";
    }


/*
This method collects memory information and returns it as a formatted string.
It retrieves the total, available, and used RAM, as well as the total and used swap memory.
It then calculates the percentage of RAM and swap used and formats this information into a human-readable string format for display in the terminal.
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
                ──────────────────────────────
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


    /*
    This method converts the collected memory information into a JSON string.
    It calculates the total, used, and available RAM and swap, as well as their usage percentages,
    and formats this data into a structured JSON format for easy consumption by other applications or services.

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

    //This method converts bytes into a human-readable format (KB, MB, GB) with two decimal places for better readability.
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