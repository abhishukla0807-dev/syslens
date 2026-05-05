/**
 * AlertEngine.java
 *
 * Author: Aman
 * Version: 1.0.0
 * Last Updated: 2026-05-05
 *
 * Description:
 *   AlertEngine is responsible for monitoring system metrics
 *   (CPU, RAM, Disk) against configurable thresholds. It parses
 *   snapshot results and triggers alerts when usage exceeds
 *   warning or critical levels.
 *
 * Purpose:
 *   - Provide real-time alerting for SysLens monitoring.
 *   - Support configurable thresholds via AppConfig.
 *   - Allow contributors to extend monitoring with additional
 *     components (e.g., GPU, Battery).
 *
 * Notes for Contributors:
 *   - Thresholds are loaded from AppConfig with sensible defaults.
 *   - Alerts are printed to console with severity levels.
 *   - Extend by adding new check methods for other components.
 *   - parsePercent() extracts numeric values from snapshot text.
 */

package com.aurexiris.syslens.monitor;

import com.aurexiris.syslens.core.SystemSnapshot;
import com.aurexiris.syslens.config.AppConfig;

import java.util.Map;

public class AlertEngine {

    // Thresholds for CPU, RAM, and Disk usage
    private double cpuWarnThreshold;
    private double cpuCritThreshold;
    private double ramWarnThreshold;
    private double ramCritThreshold;
    private double diskWarnThreshold;
    private double diskCritThreshold;

    /**
     * Constructor: loads thresholds from AppConfig.
     * Defaults are used if config values are missing.
     */
    public AlertEngine() {
        AppConfig config = AppConfig.getInstance();

        this.cpuWarnThreshold  = config.getDouble("alert.cpu.warn",     75.0);
        this.cpuCritThreshold  = config.getDouble("alert.cpu.critical", 90.0);
        this.ramWarnThreshold  = config.getDouble("alert.ram.warn",     75.0);
        this.ramCritThreshold  = config.getDouble("alert.ram.critical", 90.0);
        this.diskWarnThreshold = config.getDouble("alert.disk.warn",    80.0);
        this.diskCritThreshold = config.getDouble("alert.disk.critical",95.0);
    }

    // Setters for thresholds
    public void setCpuThresholds(double warn, double crit) {
        this.cpuWarnThreshold  = warn;
        this.cpuCritThreshold  = crit;
    }

    public void setRamThresholds(double warn, double crit) {
        this.ramWarnThreshold  = warn;
        this.ramCritThreshold  = crit;
    }

    public void setDiskThresholds(double warn, double crit) {
        this.diskWarnThreshold = warn;
        this.diskCritThreshold = crit;
    }

    /**
     * Check snapshot results against thresholds.
     * @param snapshot system snapshot containing text results
     */
    public void check(SystemSnapshot snapshot) {
        Map<String, String> results = snapshot.getTextResults();

        for (Map.Entry<String, String> entry : results.entrySet()) {
            String name    = entry.getKey();
            String content = entry.getValue();

            switch (name) {
                case "CPU Info"    -> checkCpu(content);
                case "Memory Info" -> checkRam(content);
                case "Disk Info"   -> checkDisk(content);
            }
        }
    }

    // CPU usage check
    private void checkCpu(String content) {
        double load = parsePercent(content, "CPU Load");
        if (load <= 0) return;

        if (load >= cpuCritThreshold) {
            alert("CRITICAL", "CPU",
                    String.format("%.1f%% (threshold: %.0f%%)",
                            load, cpuCritThreshold));
        } else if (load >= cpuWarnThreshold) {
            alert("WARNING", "CPU",
                    String.format("%.1f%% (threshold: %.0f%%)",
                            load, cpuWarnThreshold));
        }
    }

    // RAM usage check
    private void checkRam(String content) {
        double used = parsePercent(content, "Used RAM");
        if (used <= 0) return;

        if (used >= ramCritThreshold) {
            alert("CRITICAL", "RAM",
                    String.format("%.1f%% used (threshold: %.0f%%)",
                            used, ramCritThreshold));
        } else if (used >= ramWarnThreshold) {
            alert("WARNING", "RAM",
                    String.format("%.1f%% used (threshold: %.0f%%)",
                            used, ramWarnThreshold));
        }
    }

    // Disk usage check
    private void checkDisk(String content) {
        double used = parsePercent(content, "Used");
        if (used <= 0) return;

        if (used >= diskCritThreshold) {
            alert("CRITICAL", "DISK",
                    String.format("%.1f%% used (threshold: %.0f%%)",
                            used, diskCritThreshold));
        } else if (used >= diskWarnThreshold) {
            alert("WARNING", "DISK",
                    String.format("%.1f%% used (threshold: %.0f%%)",
                            used, diskWarnThreshold));
        }
    }

    // Utility: parse percentage values from snapshot text
    private double parsePercent(String content, String key) {
        try {
            for (String line : content.split("\n")) {
                if (line.contains(key) && line.contains("%")) {
                    String part = line.substring(line.lastIndexOf(':') + 1).trim();
                    String num = part.replaceAll("[^0-9.]", "");
                    return Double.parseDouble(num);
                }
            }
        } catch (Exception ignored) {}
        return -1;
    }

    // Utility: print alert message
    private void alert(String level, String component, String detail) {
        System.out.printf("%n%s [%s] %s%n", level, component, detail);
    }
}
