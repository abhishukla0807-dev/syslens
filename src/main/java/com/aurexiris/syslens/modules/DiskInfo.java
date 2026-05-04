/**
 * DiskInfo
 * Module to collect disk partition details including total,
 * used, free space and usage percentage.
 */


package com.aurexiris.syslens.modules;

import com.aurexiris.syslens.core.InfoCollector;
import oshi.SystemInfo;
import oshi.software.os.OSFileStore;
import oshi.software.os.OperatingSystem;

import java.util.List;

public class DiskInfo implements InfoCollector {

    private final OperatingSystem os =
            new SystemInfo().getOperatingSystem();


    // This method returns the name of this collector, which is used as a key in the snapshot.
    @Override
    public String getName() {
        return "Disk Info";
    }

    // This method collects disk information and returns it as a formatted string.
    // It retrieves the list of file stores (partitions) from the operating system, then iterates through.
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


            // This method formats the disk information for each file store and appends it to the StringBuilder.
            // It includes the mount point, name, label, type, total space, used space (with percentage), and free space.
            //The Syntax of this method is "sb.append("""...""".formatted(...))"  which allows for multi-line string formatting.
            sb.append("""
                    Drive       : %s (%s)
                    Label       : %s
                    Type        : %s
                    Total       : %s
                    Used        : %s (%.1f%%)
                    Free        : %s
                    ──────────────────────────────
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

        sb.append("  ]");
        return sb.toString();
    }

    // This helper method formats byte values into human-readable strings (e.g., KB, MB, GB) with two decimal places.
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