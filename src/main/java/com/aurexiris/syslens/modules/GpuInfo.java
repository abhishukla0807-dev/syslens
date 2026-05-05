/**
 * GpuInfo.java
 *
 * Description:
 *   GpuInfo is an InfoCollector module that retrieves GPU details
 *   using OSHI's GraphicsCard API. It reports information such as
 *   name, vendor, VRAM size, driver version, and device ID.
 *
 * Purpose:
 *   - Provide detailed GPU monitoring for SysLens.
 *   - Support both human-readable output (collect()) and JSON export (toJson()).
 *   - Allow contributors to extend monitoring with additional GPU metrics
 *     such as temperature, utilization, or multiple GPU support.
 *
 * Notes for Contributors:
 *   - It uses OSHI's SystemInfo.getHardware().getGraphicsCards().
 *   - If no GPU is detected, outputs a clear message.
 *   - Extend by adding new fields or integrating with other GPU APIs.
 *   - Use formatBytes() helper for consistent VRAM display.
 */

package com.aurexiris.syslens.modules;

import com.aurexiris.syslens.core.InfoCollector;
import oshi.SystemInfo;
import oshi.hardware.GraphicsCard;

import java.util.List;

public class GpuInfo implements InfoCollector {

    // List of detected graphics cards
    private final List<GraphicsCard> gpus =
            new SystemInfo().getHardware().getGraphicsCards();

    @Override
    public String getName() {
        return "GPU Info";
    }

    /**
     * Collect GPU information in a human-readable format.
     * Includes name, vendor, VRAM, version, and device ID.
     * @return formatted string with GPU details
     */
    @Override
    public String collect() {
        if (gpus.isEmpty()) {
            return """
                    ==============================
                         GPU INFORMATION
                    ==============================
                    No GPU detected
                    ==============================
                    """;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("""
                ==============================
                       GPU INFORMATION
                ==============================
                """);

        for (GraphicsCard gpu : gpus) {
            sb.append("""
                    Name        : %s
                    Vendor      : %s
                    VRAM        : %s
                    Version     : %s
                    Device ID   : %s
                    ------------------------------
                    """.formatted(
                    gpu.getName(),
                    gpu.getVendor(),
                    formatBytes(gpu.getVRam()),
                    gpu.getVersionInfo(),
                    gpu.getDeviceId()
            ));
        }

        sb.append("==============================");
        return sb.toString();
    }

    /**
     * Export GPU information as JSON.
     * @return JSON string with GPU details
     */
    @Override
    public String toJson() {
        if (gpus.isEmpty()) {
            return """
                    {
                      "gpuDetected": false
                    }""";
        }

        StringBuilder sb = new StringBuilder("[\n");

        for (int i = 0; i < gpus.size(); i++) {
            GraphicsCard gpu = gpus.get(i);

            sb.append("""
                      {
                        "name": "%s",
                        "vendor": "%s",
                        "vramBytes": %d,
                        "vramFormatted": "%s",
                        "version": "%s",
                        "deviceId": "%s"
                      }""".formatted(
                    gpu.getName(),
                    gpu.getVendor(),
                    gpu.getVRam(),
                    formatBytes(gpu.getVRam()),
                    gpu.getVersionInfo(),
                    gpu.getDeviceId()
            ));

            if (i < gpus.size() - 1) sb.append(",");
            sb.append("\n");
        }

        sb.append("]");
        return sb.toString();
    }

    // Helper method to format VRAM size into human-readable strings
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
