/**
 * SensorInfo.java
 *
 * Author: Abhishek Shukla
 * Version: 1.0.0
 * Last Updated: 2026-05-05
 *
 * Description:
 *   SensorInfo is an InfoCollector module that retrieves hardware sensor
 *   data using jSensors when available, with an OSHI-based fallback.
 *   It reports CPU and GPU temperatures, fan speeds, and CPU voltage,
 *   and exposes both human-readable (collect) and JSON (toJson) outputs.
 *
 * Purpose:
 *   - Provide detailed sensor telemetry for SysLens (temperatures, fans, voltage).
 *   - Prefer jSensors for rich sensor data; fall back to OSHI when jSensors
 *     cannot access sensors (e.g., limited permissions).
 *   - Offer both console-friendly text and structured JSON for downstream tools.
 *
 * Notes for Contributors:
 *   - jSensors is used first (JSensors.get.components()); OSHI Sensors is used
 *     as a fallback when jSensors returns no real data or throws an exception.
 *   - The helper hasRealData(...) checks for meaningful temperature values.
 *   - formatTemp, formatVoltage, and formatFans provide consistent, localized
 *     formatting and human-friendly severity labels.
 *   - When running on Windows, elevated privileges or a background service
 *     (e.g., LibreHardwareMonitor) may be required for full sensor access.
 */

package com.aurexiris.syslens.modules;

import com.aurexiris.syslens.core.InfoCollector;
import com.profesorfalken.jsensors.JSensors;
import com.profesorfalken.jsensors.model.components.Components;
import com.profesorfalken.jsensors.model.components.Cpu;
import com.profesorfalken.jsensors.model.components.Gpu;
import com.profesorfalken.jsensors.model.sensors.Fan;
import com.profesorfalken.jsensors.model.sensors.Temperature;
import oshi.SystemInfo;
import oshi.hardware.Sensors;

import java.util.List;

public class SensorInfo implements InfoCollector {

    // OSHI fallback sensors (used when jSensors is unavailable or returns no data)
    private final Sensors oshiSensors =
            new SystemInfo().getHardware().getSensors();

    @Override
    public String getName() {
        return "Sensor Info";
    }

    /**
     * Collect sensor information in a human-readable format.
     * Tries jSensors first for rich CPU/GPU sensor data; falls back to OSHI.
     *
     * @return formatted string with sensor details
     */
    @Override
    public String collect() {
        StringBuilder sb = new StringBuilder();
        sb.append("""
                ==============================
                      SENSOR INFORMATION
                ==============================
                """);

        // Try jSensors first for the most complete sensor data
        boolean jSensorsWorked = false;

        try {
            Components components = JSensors.get.components();
            boolean hasData = hasRealData(components);

            if (hasData) {
                jSensorsWorked = true;

                // CPU sensors (temperatures, fans)
                if (components.cpus != null && !components.cpus.isEmpty()) {
                    for (Cpu cpu : components.cpus) {
                        sb.append("CPU         : ").append(cpu.name).append("\n");

                        // Temperatures
                        if (cpu.sensors != null
                                && cpu.sensors.temperatures != null
                                && !cpu.sensors.temperatures.isEmpty()) {
                            for (Temperature t : cpu.sensors.temperatures) {
                                if (t.value != null && t.value > 0) {
                                    sb.append(String.format(
                                            "  Temp %-12s: %s%n",
                                            t.name,
                                            formatTemp(t.value)
                                    ));
                                }
                            }
                        } else {
                            sb.append("  Temp        : N/A\n");
                        }

                        // Fans
                        if (cpu.sensors != null
                                && cpu.sensors.fans != null
                                && !cpu.sensors.fans.isEmpty()) {
                            for (Fan f : cpu.sensors.fans) {
                                if (f.value != null && f.value > 0) {
                                    sb.append(String.format(
                                            "  Fan  %-12s: %.0f RPM%n",
                                            f.name, f.value
                                    ));
                                }
                            }
                        } else {
                            sb.append("  Fans        : N/A\n");
                        }

                        sb.append("──────────────────────────────\n");
                    }
                }

                // GPU sensors (temperatures, fans)
                if (components.gpus != null && !components.gpus.isEmpty()) {
                    for (Gpu gpu : components.gpus) {
                        sb.append("GPU         : ").append(gpu.name).append("\n");

                        if (gpu.sensors != null
                                && gpu.sensors.temperatures != null
                                && !gpu.sensors.temperatures.isEmpty()) {
                            for (Temperature t : gpu.sensors.temperatures) {
                                if (t.value != null && t.value > 0) {
                                    sb.append(String.format(
                                            "  Temp %-12s: %s%n",
                                            t.name,
                                            formatTemp(t.value)
                                    ));
                                }
                            }
                        } else {
                            sb.append("  Temp        : N/A\n");
                        }

                        if (gpu.sensors != null
                                && gpu.sensors.fans != null
                                && !gpu.sensors.fans.isEmpty()) {
                            for (Fan f : gpu.sensors.fans) {
                                if (f.value != null && f.value > 0) {
                                    sb.append(String.format(
                                            "  Fan  %-12s: %.0f RPM%n",
                                            f.name, f.value
                                    ));
                                }
                            }
                        } else {
                            sb.append("  Fans        : N/A\n");
                        }

                        sb.append("──────────────────────────────\n");
                    }
                }
            }

        } catch (Exception ignored) {
            // If jSensors throws, we'll fall back to OSHI below
            jSensorsWorked = false;
        }

        // OSHI fallback if jSensors provided no usable data
        if (!jSensorsWorked) {
            double cpuTemp   = oshiSensors.getCpuTemperature();
            double cpuVolt   = oshiSensors.getCpuVoltage();
            int[]  fanSpeeds = oshiSensors.getFanSpeeds();

            sb.append(String.format("CPU Temp    : %s%n", formatTemp(cpuTemp)));
            sb.append(String.format("CPU Voltage : %s%n", formatVoltage(cpuVolt)));
            sb.append(formatFans(fanSpeeds)).append("\n");
            sb.append("──────────────────────────────\n");
            sb.append("""
                    ℹ️  Limited sensor access.
                    └─ For full data: Run as Admin
                    └─ Or run LibreHardwareMonitor
                       as background service
                    """);
        }

        sb.append("==============================");
        return sb.toString();
    }

    /**
     * Export sensor information as JSON.
     * Uses jSensors when available; otherwise returns an OSHI-based fallback JSON.
     *
     * @return JSON string with sensor details
     */
    @Override
    public String toJson() {
        try {
            Components components = JSensors.get.components();
            boolean hasData = hasRealData(components);

            if (!hasData) {
                // OSHI fallback JSON
                double cpuTemp = oshiSensors.getCpuTemperature();
                double cpuVolt = oshiSensors.getCpuVoltage();
                int[] fans     = oshiSensors.getFanSpeeds();

                StringBuilder fanArr = new StringBuilder("[");
                for (int i = 0; i < fans.length; i++) {
                    fanArr.append(fans[i]);
                    if (i < fans.length - 1) fanArr.append(",");
                }
                fanArr.append("]");

                return """
                        {
                          "source": "oshi-fallback",
                          "cpuTemperatureCelsius": %.1f,
                          "cpuVoltage": %.2f,
                          "fanSpeedsRpm": %s
                        }""".formatted(cpuTemp, cpuVolt, fanArr);
            }

            // jSensors JSON output
            StringBuilder sb = new StringBuilder("{\n");
            sb.append("  \"source\": \"jsensors\",\n");

            // CPU array
            sb.append("  \"cpu\": [\n");
            if (components.cpus != null) {
                for (int i = 0; i < components.cpus.size(); i++) {
                    Cpu cpu = components.cpus.get(i);
                    sb.append("    {\n");
                    sb.append(String.format("      \"name\": \"%s\",%n", cpu.name));

                    // temperatures
                    sb.append("      \"temperatures\": [");
                    if (cpu.sensors != null && cpu.sensors.temperatures != null) {
                        List<Temperature> temps = cpu.sensors.temperatures;
                        for (int j = 0; j < temps.size(); j++) {
                            Temperature t = temps.get(j);
                            if (t.value != null) {
                                sb.append(String.format(
                                        "{\"name\":\"%s\",\"celsius\":%.1f}",
                                        t.name, t.value));
                                if (j < temps.size() - 1) sb.append(",");
                            }
                        }
                    }
                    sb.append("],\n");

                    // fans
                    sb.append("      \"fans\": [");
                    if (cpu.sensors != null && cpu.sensors.fans != null) {
                        List<Fan> fans = cpu.sensors.fans;
                        for (int j = 0; j < fans.size(); j++) {
                            Fan f = fans.get(j);
                            if (f.value != null) {
                                sb.append(String.format(
                                        "{\"name\":\"%s\",\"rpm\":%.0f}",
                                        f.name, f.value));
                                if (j < fans.size() - 1) sb.append(",");
                            }
                        }
                    }
                    sb.append("]\n    }");
                    if (i < components.cpus.size() - 1) sb.append(",");
                    sb.append("\n");
                }
            }
            sb.append("  ],\n");

            // GPU array
            sb.append("  \"gpu\": [\n");
            if (components.gpus != null) {
                for (int i = 0; i < components.gpus.size(); i++) {
                    Gpu gpu = components.gpus.get(i);
                    sb.append("    {\n");
                    sb.append(String.format("      \"name\": \"%s\",%n", gpu.name));

                    sb.append("      \"temperatures\": [");
                    if (gpu.sensors != null && gpu.sensors.temperatures != null) {
                        List<Temperature> temps = gpu.sensors.temperatures;
                        for (int j = 0; j < temps.size(); j++) {
                            Temperature t = temps.get(j);
                            if (t.value != null) {
                                sb.append(String.format(
                                        "{\"name\":\"%s\",\"celsius\":%.1f}",
                                        t.name, t.value));
                                if (j < temps.size() - 1) sb.append(",");
                            }
                        }
                    }
                    sb.append("]\n    }");
                    if (i < components.gpus.size() - 1) sb.append(",");
                    sb.append("\n");
                }
            }
            sb.append("  ]\n}");
            return sb.toString();

        } catch (Exception e) {
            return """
                    {
                      "error": "Sensor access failed",
                      "reason": "%s"
                    }""".formatted(e.getMessage());
        }
    }

    // ── helpers ──────────────────────────────────────────────

    /**
     * Determine whether jSensors returned meaningful sensor data.
     * Checks CPU and GPU temperature lists for positive values.
     */
    private boolean hasRealData(Components components) {
        if (components == null) return false;

        // check CPU temps
        if (components.cpus != null) {
            for (Cpu cpu : components.cpus) {
                if (cpu.sensors != null
                        && cpu.sensors.temperatures != null) {
                    for (Temperature t : cpu.sensors.temperatures) {
                        if (t.value != null && t.value > 0) return true;
                    }
                }
            }
        }

        // check GPU temps
        if (components.gpus != null) {
            for (Gpu gpu : components.gpus) {
                if (gpu.sensors != null
                        && gpu.sensors.temperatures != null) {
                    for (Temperature t : gpu.sensors.temperatures) {
                        if (t.value != null && t.value > 0) return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * Format a temperature value into a human-friendly string with severity.
     * Returns "N/A" for non-positive values.
     */
    private String formatTemp(double temp) {
        if (temp <= 0) return "N/A";
        String level = temp < 50 ? "✅ Cool" :
                temp < 70 ? "⚠️  Warm" :
                        temp < 85 ? "🔥 Hot" :
                                "🚨 Critical";
        return String.format("%.1f°C  %s", temp, level);
    }

    /**
     * Format CPU voltage; returns an explanatory message when unavailable.
     */
    private String formatVoltage(double volt) {
        if (volt <= 0) return "N/A (run as admin)";
        return String.format("%.2f V", volt);
    }

    /**
     * Format fan speeds array into readable lines. Returns N/A message when empty.
     */
    private String formatFans(int[] fans) {
        if (fans == null || fans.length == 0)
            return "Fans       : N/A (run as admin)";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < fans.length; i++) {
            sb.append(String.format("Fan %-2d      : %d RPM%n", i + 1, fans[i]));
        }
        return sb.toString().stripTrailing();
    }

    /**
     * Helper message for missing sensor access for a specific component.
     */
    private String noSensorMsg(String component) {
        return """
                %s Sensors  : N/A
                └─ Run as Administrator for sensor access
                """.formatted(component);
    }
}
