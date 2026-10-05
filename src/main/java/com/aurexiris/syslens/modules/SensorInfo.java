/**
 * SensorInfo.java
 *
 * Author: Abhishek Shukla
 * Version: 1.2.0
 * Last Updated: 2026-10-05
 *
 * Description:
 *   SensorInfo is an InfoCollector module that retrieves hardware telemetry
 *   (CPU temperature and voltage) by combining LibreHardwareMonitor
 *   (via jLibreHardwareMonitor) with OSHI fallback.
 *
 * Purpose:
 *   - Provide real-time hardware thermal and voltage monitoring.
 *   - Leverage LibreHardwareMonitor's native kernel driver interface on Windows
 *     to query CPU MSRs for core temperature and core voltage.
 *   - Gracefully fallback to OSHI on Linux, macOS, or unprivileged sessions.
 *   - Export sensor data in human-readable and structured JSON formats.
 */

package com.aurexiris.syslens.modules;

import com.aurexiris.syslens.core.InfoCollector;
import io.github.pandalxb.jlibrehardwaremonitor.config.ComputerConfig;
import io.github.pandalxb.jlibrehardwaremonitor.manager.LibreHardwareManager;
import io.github.pandalxb.jlibrehardwaremonitor.model.Computer;
import io.github.pandalxb.jlibrehardwaremonitor.model.Hardware;
import io.github.pandalxb.jlibrehardwaremonitor.model.Sensor;
import oshi.SystemInfo;
import oshi.hardware.Sensors;

import java.util.ArrayList;
import java.util.List;

public class SensorInfo implements InfoCollector {

    // Reference to hardware sensors via OSHI (fallback and cross-platform)
    private final Sensors oshiSensors = new SystemInfo().getHardware().getSensors();

    @Override
    public String getName() {
        return "Hardware Sensors";
    }

    /**
     * Collect hardware sensor telemetry in human-readable format.
     * Includes CPU temperature and CPU voltage.
     *
     * @return formatted string with hardware sensor details
     */
    @Override
    public String collect() {
        SensorData data = readSensors();

        String tempStr;
        if (data.cpuTempC != null && data.cpuTempC > 0.0) {
            double tempF = (data.cpuTempC * 9.0 / 5.0) + 32.0;
            tempStr = String.format("%.1f°C (%.1f°F)", data.cpuTempC, tempF);
        } else {
            tempStr = "N/A (not supported / run as Admin)";
        }

        String voltageStr = (data.cpuVoltage != null && data.cpuVoltage > 0.0)
                ? String.format("%.2f V", data.cpuVoltage)
                : "N/A";

        return """
                ==============================
                       HARDWARE SENSORS
                ==============================
                CPU Temp    : %s
                CPU Voltage : %s
                ==============================
                """.formatted(tempStr, voltageStr);
    }

    /**
     * Export hardware sensor telemetry as JSON.
     *
     * @return JSON string with sensor metrics
     */
    @Override
    public String toJson() {
        SensorData data = readSensors();

        Double tempF = (data.cpuTempC != null && data.cpuTempC > 0.0)
                ? ((data.cpuTempC * 9.0 / 5.0) + 32.0)
                : null;

        return """
                {
                  "cpuTemperatureC": %s,
                  "cpuTemperatureF": %s,
                  "cpuVoltage": %s
                }""".formatted(
                data.cpuTempC != null ? String.format("%.1f", data.cpuTempC) : "null",
                tempF != null ? String.format("%.1f", tempF) : "null",
                data.cpuVoltage != null ? String.format("%.2f", data.cpuVoltage) : "null"
        );
    }

    /**
     * Aggregates readings by first querying LibreHardwareMonitor on Windows,
     * and falling back to OSHI if unavailable or missing metrics.
     */
    private SensorData readSensors() {
        Double tempC = null;
        Double voltage = null;

        // 1. Attempt LibreHardwareMonitor on Windows
        if (isWindows()) {
            try {
                Computer computer = LibreHardwareManager.createInstance(
                        ComputerConfig.getInstance().enableAll()
                ).getComputer();

                List<Hardware> allHardware = new ArrayList<>();
                if (computer != null && computer.getHardware() != null) {
                    for (Hardware h : computer.getHardware()) {
                        collectHardwareRecursive(h, allHardware);
                    }
                }

                for (Hardware h : allHardware) {
                    String hwType = h.getHardwareType() != null ? h.getHardwareType() : "";
                    boolean isCpu = hwType.equalsIgnoreCase("Cpu");

                    if (h.getSensors() != null) {
                        for (Sensor s : h.getSensors()) {
                            String type = s.getSensorType() != null ? s.getSensorType() : "";
                            String name = s.getName() != null ? s.getName() : "";
                            double val = s.getValue();

                            // Voltage
                            if (type.equalsIgnoreCase("Voltage") && val > 0.0) {
                                if (isCpu || name.toLowerCase().contains("vcore") || name.toLowerCase().contains("cpu")) {
                                    if (voltage == null) {
                                        voltage = val;
                                    }
                                }
                            }

                            // Temperature
                            if (type.equalsIgnoreCase("Temperature") && val > 0.0) {
                                if (name.equalsIgnoreCase("CPU Package") || name.equalsIgnoreCase("Core Max")) {
                                    tempC = val;
                                } else if (tempC == null && (isCpu || name.toLowerCase().contains("cpu"))) {
                                    tempC = val;
                                }
                            }
                        }
                    }
                }
            } catch (Throwable ignored) {
                // Fall back gracefully to OSHI
            }
        }

        // 2. Fallbacks from OSHI
        if (tempC == null || tempC <= 0.0) {
            double oshiTemp = oshiSensors.getCpuTemperature();
            if (oshiTemp > 0.0) {
                tempC = oshiTemp;
            }
        }

        if (voltage == null || voltage <= 0.0) {
            double oshiVolts = oshiSensors.getCpuVoltage();
            if (oshiVolts > 0.0) {
                voltage = oshiVolts;
            }
        }

        return new SensorData(tempC, voltage);
    }

    private void collectHardwareRecursive(Hardware h, List<Hardware> out) {
        if (h == null) return;
        out.add(h);
        if (h.getSubHardware() != null) {
            for (Hardware sub : h.getSubHardware()) {
                collectHardwareRecursive(sub, out);
            }
        }
    }

    private boolean isWindows() {
        return System.getProperty("os.name", "").toLowerCase().contains("win");
    }

    private static class SensorData {
        final Double cpuTempC;
        final Double cpuVoltage;

        SensorData(Double cpuTempC, Double cpuVoltage) {
            this.cpuTempC = cpuTempC;
            this.cpuVoltage = cpuVoltage;
        }
    }
}
