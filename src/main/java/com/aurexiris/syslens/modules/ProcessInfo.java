/**
 * ProcessInfo.java
 *
 * Author: Aman
 * Version: 1.0.0
 * Last Updated: 2026-05-05
 *
 * Description:
 *   ProcessInfo is an InfoCollector module that retrieves details
 *   about running processes using OSHI's OperatingSystem API. It reports
 *   the total number of processes and provides a snapshot of the top
 *   processes sorted by CPU usage.
 *
 * Purpose:
 *   - Provide visibility into active processes for SysLens monitoring.
 *   - Support both human-readable output (collect()) and JSON export (toJson()).
 *   - Allow contributors to extend monitoring with additional process metrics
 *     such as thread counts, I/O statistics, or per-user breakdowns.
 *
 * Notes for Contributors:
 *   - The number of processes displayed is configurable via AppConfig
 *     (property: process.top.count).
 *   - Uses OSHI's OperatingSystem.getProcesses() with CPU_DESC sorting.
 *   - Extend by adding new fields or integrating with other OSHI APIs.
 *   - Helpers (calculateCpu, getMemory, formatBytes, truncate, escape)
 *     ensure consistent output formatting and safe string handling.
 */

package com.aurexiris.syslens.modules;

import com.aurexiris.syslens.config.AppConfig;
import com.aurexiris.syslens.core.InfoCollector;
import oshi.SystemInfo;
import oshi.software.os.OSProcess;
import oshi.software.os.OperatingSystem;

import java.util.List;

public class ProcessInfo implements InfoCollector {

    // Reference to the operating system via OSHI
    private final OperatingSystem os =
            new SystemInfo().getOperatingSystem();

    // Configurable number of top processes to display
    private final int TOP_COUNT = AppConfig.getInstance()
            .getInt("process.top.count", 10);

    @Override
    public String getName() {
        return "Process Info";
    }

    /**
     * Collect process information in a human-readable format.
     * Includes total process count and top processes sorted by CPU usage.
     * @return formatted string with process details
     */
    @Override
    public String collect() {
        List<OSProcess> processes = os.getProcesses(
                OperatingSystem.ProcessFiltering.ALL_PROCESSES,
                OperatingSystem.ProcessSorting.CPU_DESC,
                TOP_COUNT
        );

        StringBuilder sb = new StringBuilder();
        sb.append("""
                ==============================
                     PROCESS INFORMATION
                ==============================
                Total Processes : %d
                ------------------------------
                """.formatted(os.getProcessCount()));

        sb.append(String.format("%-6s %-25s %-10s %-12s %s%n",
                "PID", "Name", "CPU%", "Memory", "Status"));
        sb.append("------------------------------------------\n");

        for (OSProcess p : processes) {
            double cpuPct = calculateCpu(p);
            long   memory = getMemory(p);

            sb.append(String.format("%-6d %-25s %-10.1f %-12s %s%n",
                    p.getProcessID(),
                    truncate(p.getName(), 25),
                    cpuPct,
                    formatBytes(memory),
                    p.getState().name()
            ));
        }

        sb.append("==============================");
        return sb.toString();
    }

    /**
     * Export process information as JSON.
     * @return JSON string with process details
     */
    @Override
    public String toJson() {
        List<OSProcess> processes = os.getProcesses(
                OperatingSystem.ProcessFiltering.ALL_PROCESSES,
                OperatingSystem.ProcessSorting.CPU_DESC,
                TOP_COUNT
        );

        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append(String.format("  \"totalProcesses\": %d,%n",
                os.getProcessCount()));
        sb.append("  \"topByCpu\": [\n");

        for (int i = 0; i < processes.size(); i++) {
            OSProcess p      = processes.get(i);
            double    cpuPct = calculateCpu(p);
            long      memory = getMemory(p);

            sb.append("""
                      {
                        "pid": %d,
                        "name": "%s",
                        "cpuPercent": %.1f,
                        "memoryBytes": %d,
                        "memoryFormatted": "%s",
                        "status": "%s"
                      }""".formatted(
                    p.getProcessID(),
                    escape(p.getName()),
                    cpuPct,
                    memory,
                    formatBytes(memory),
                    p.getState().name()
            ));

            if (i < processes.size() - 1) sb.append(",");
            sb.append("\n");
        }

        sb.append("  ]\n}");
        return sb.toString();
    }

    // Helper to calculate CPU usage percentage
    private double calculateCpu(OSProcess p) {
        long upTime = p.getUpTime();
        if (upTime == 0) return 0.0;
        return 100d * (p.getKernelTime() + p.getUserTime()) / upTime;
    }

    // Helper to retrieve memory usage safely
    private long getMemory(OSProcess p) {
        try {
            return p.getVirtualSize();
        } catch (Exception e) {
            return 0L;
        }
    }

    // Helper to format byte values into human-readable strings
    private String formatBytes(long bytes) {
        if (bytes <= 0) return "N/A";
        if (bytes >= 1_073_741_824L)
            return String.format("%.2f GB", bytes / 1_073_741_824.0);
        if (bytes >= 1_048_576L)
            return String.format("%.2f MB", bytes / 1_048_576.0);
        if (bytes >= 1_024L)
            return String.format("%.2f KB", bytes / 1_024.0);
        return bytes + " B";
    }

    // Helper to truncate long process names
    private String truncate(String text, int max) {
        if (text == null) return "N/A";
        return text.length() > max
                ? text.substring(0, max - 1) + "…"
                : text;
    }

    // Helper to escape special characters for JSON output
    private String escape(String text) {
        if (text == null) return "";
        return text.replace("\"", "\\\"")
                .replace("\\", "\\\\");
    }
}
