/**
 * DiskInfo.java
 *
 * Description:
 *   DiskInfo is an InfoCollector module that retrieves details about
 *   disk partitions using OSHI's OSFileStore API. It reports total,
 *   used, and free space for each partition, along with usage percentage.
 *
 * Purpose:
 *   - Provide detailed disk usage monitoring for SysLens.
 *   - Support both human-readable output (collect()) and JSON export (toJson()).
 *   - Allow contributors to extend monitoring with additional disk metrics
 *     such as I/O statistics or filesystem attributes.
 *
 * Notes for Contributors:
 *   - Use OSHI's OperatingSystem.getFileSystem().getFileStores(true).
 *   - If labels are missing, "N/A" is used for clarity.
 *   - Extend by adding new fields or integrating with other storage APIs.
 *   - Use formatBytes() helper for consistent human-readable output.
 */

package com.aurexiris.syslens.modules;

import com.aurexiris.syslens.core.InfoCollector;
import oshi.SystemInfo;
import oshi.software.os.OSFileStore;
import oshi.software.os.OperatingSystem;

import java.util.List;

public class DiskInfo implements InfoCollector {

    // Reference to the operating system via OSHI
    private final OperatingSystem os =
            new SystemInfo().getOperatingSystem();

    @Override
    public String getName() {
        return "Disk Info";
    }

    /**
     * Collect disk information in a human-readable format.
     * Includes mount point, label, type, total, used, and free space.
     * @return formatted string with disk details
     */
    @Override
    public String collect() {
        List<OSFileStore> stores = os.getFileSystem().getFileStores(true);

        StringBuilder sb = new StringBuilder();
        sb.append("""
                ==============================
                       DISK INFORMATION
                ==============================
                """);

        for (OSFileStore store : stores) {
            long total     = store.getTotalSpace();
            long free      = store.getUsableSpace();
            long used      = total - free;
            double usedPct = total > 0 ? (used * 100.0) / total : 0.0;

            sb.append("""
                    Drive       : %s (%s)
                    Label       : %s
                    Type        : %s
                    Total       : %s
                    Used        : %s (%.1f%%)
                    Free        : %s
                    ------------------------------
                    """.formatted(
                    store.getMount(),
                    store.getName(),
                    store.getLabel().isEmpty() ? "N/A" : store.getLabel(),
                    store.getType(),
                    formatBytes(total),
                    formatBytes(used), usedPct,
                    formatBytes(free)
            ));
        }

        sb.append("==============================");
        return sb.toString();
    }

    /**
     * Export disk information as JSON.
     * @return JSON string with disk details
     */
    @Override
    public String toJson() {
        List<OSFileStore> stores = os.getFileSystem().getFileStores(true);

        StringBuilder sb = new StringBuilder("[\n");

        for (int i = 0; i < stores.size(); i++) {
            OSFileStore store = stores.get(i);
            long total    = store.getTotalSpace();
            long free     = store.getUsableSpace();
            long used     = total - free;
            double usedPct = total > 0 ? (used * 100.0) / total : 0.0;

            sb.append("""
                      {
                        "mount": "%s",
                        "name": "%s",
                        "label": "%s",
                        "type": "%s",
                        "totalBytes": %d,
                        "usedBytes": %d,
                        "freeBytes": %d,
                        "usedPercent": %.1f
                      }""".formatted(
                    store.getMount(),
                    store.getName(),
                    store.getLabel().isEmpty() ? "N/A" : store.getLabel(),
                    store.getType(),
                    total, used, free, usedPct
            ));

            if (i < stores.size() - 1) sb.append(",");
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
}
