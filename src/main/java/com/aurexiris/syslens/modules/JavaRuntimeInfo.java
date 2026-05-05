/**
 * JavaRuntimeInfo.java
 *
 * Description:
 *   JavaRuntimeInfo is an InfoCollector module that retrieves details
 *   about the Java runtime environment and JVM memory usage. It reports
 *   Java version, vendor, VM details, heap statistics, and system paths.
 *
 * Purpose:
 *   - Provide visibility into the Java runtime environment used by SysLens.
 *   - Support both human-readable output (collect()) and JSON export (toJson()).
 *   - Allow contributors to extend monitoring with additional JVM metrics
 *     such as GC statistics, thread counts, or system properties.
 *
 * Notes for Contributors:
 *   - Uses the built-in Runtime class and System properties.
 *   - Heap values are reported in bytes and formatted for readability.
 *   - Extend by adding new fields or integrating with JMX for advanced metrics.
 *   - Use formatBytes() helper for consistent memory display.
 */

package com.aurexiris.syslens.modules;

import com.aurexiris.syslens.core.InfoCollector;

public class JavaRuntimeInfo implements InfoCollector {

    // Reference to the JVM runtime
    private final Runtime runtime = Runtime.getRuntime();

    @Override
    public String getName() {
        return "Java Runtime Info";
    }

    /**
     * Collect Java runtime information in a human-readable format.
     * Includes Java version, vendor, VM details, heap usage, and system paths.
     * @return formatted string with runtime details
     */
    @Override
    public String collect() {
        long maxHeap   = runtime.maxMemory();
        long totalHeap = runtime.totalMemory();
        long freeHeap  = runtime.freeMemory();
        long usedHeap  = totalHeap - freeHeap;

        return """
                ==============================
                   JAVA RUNTIME INFORMATION
                ==============================
                Java Version : %s
                Java Vendor  : %s
                JVM Name     : %s
                JVM Version  : %s
                Class Version: %s
                ------------------------------
                Max Heap     : %s
                Total Heap   : %s
                Used Heap    : %s
                Free Heap    : %s
                ------------------------------
                CPU Cores    : %d
                Temp Dir     : %s
                User Home    : %s
                Working Dir  : %s
                ==============================
                """.formatted(
                System.getProperty("java.version"),
                System.getProperty("java.vendor"),
                System.getProperty("java.vm.name"),
                System.getProperty("java.vm.version"),
                System.getProperty("java.class.version"),
                formatBytes(maxHeap),
                formatBytes(totalHeap),
                formatBytes(usedHeap),
                formatBytes(freeHeap),
                runtime.availableProcessors(),
                System.getProperty("java.io.tmpdir"),
                System.getProperty("user.home"),
                System.getProperty("user.dir")
        );
    }

    /**
     * Export Java runtime information as JSON.
     * @return JSON string with runtime details
     */
    @Override
    public String toJson() {
        long maxHeap   = runtime.maxMemory();
        long totalHeap = runtime.totalMemory();
        long freeHeap  = runtime.freeMemory();
        long usedHeap  = totalHeap - freeHeap;

        return """
                {
                  "java": {
                    "version": "%s",
                    "vendor": "%s",
                    "vmName": "%s",
                    "vmVersion": "%s",
                    "classVersion": "%s"
                  },
                  "heap": {
                    "maxBytes": %d,
                    "totalBytes": %d,
                    "usedBytes": %d,
                    "freeBytes": %d
                  },
                  "system": {
                    "availableCores": %d,
                    "tempDir": "%s",
                    "userHome": "%s",
                    "workingDir": "%s"
                  }
                }""".formatted(
                System.getProperty("java.version"),
                System.getProperty("java.vendor"),
                System.getProperty("java.vm.name"),
                System.getProperty("java.vm.version"),
                System.getProperty("java.class.version"),
                maxHeap, totalHeap, usedHeap, freeHeap,
                runtime.availableProcessors(),
                System.getProperty("java.io.tmpdir"),
                System.getProperty("user.home"),
                System.getProperty("user.dir")
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
