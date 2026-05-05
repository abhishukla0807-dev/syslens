/**
 * BatteryInfo.java
 *
 * Description:
 *   BatteryInfo is an InfoCollector module that retrieves battery
 *   statistics using OSHI's PowerSource API. It reports details such
 *   as capacity, voltage, amperage, power usage, cycle count, chemistry,
 *   and estimated time remaining.
 *
 * Purpose:
 *   - Provide detailed battery monitoring for SysLens.
 *   - Support both human-readable outputs.
 *   - Allow contributors to extend monitoring with additional battery metrics.
 *
 * Notes for Contributors:
 *   - Uses OSHI's PowerSource objects to query battery attributes.
 *   - If no battery is detected, outputs a clear message (common on desktops).
 *   - Extend by adding new fields or integrating with other sensor libraries.
 *   - Ensure formatTime() helper is used for consistent time display.
 */

package com.aurexiris.syslens.modules;

import com.aurexiris.syslens.core.InfoCollector;
import oshi.SystemInfo;
import oshi.hardware.PowerSource;

import java.util.List;

public class BatteryInfo implements InfoCollector {

    // List of detected power sources (batteries)
    private final List<PowerSource> batteries =
            new SystemInfo().getHardware().getPowerSources();

    @Override
    public String getName() {
        return "Battery Info";
    }

    /**
     * Collect battery information in a human-readable format.
     * @return formatted string with battery details
     */
    @Override
    public String collect() {
        if (batteries.isEmpty()) {
            return """
                    ==============================
                         BATTERY INFORMATION
                    ==============================
                    No battery detected (Desktop PC)
                    ==============================
                    """;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("""
                ==============================
                     BATTERY INFORMATION
                ==============================
                """);

        for (PowerSource battery : batteries) {
            battery.updateAttributes();

            double capacity   = battery.getRemainingCapacityPercent() * 100;
            double voltage    = battery.getVoltage();
            double amperage   = battery.getAmperage();
            double wattage    = battery.getPowerUsageRate();
            int    cycleCount = battery.getCycleCount();

            String status = battery.isCharging()    ? "Charging" :
                    battery.isDischarging() ? "Discharging" :
                            "Full / Plugged";

            String timeLeft = battery.isDischarging() && battery.getTimeRemainingEstimated() > 0
                    ? formatTime((long) battery.getTimeRemainingEstimated())
                    : "N/A";

            sb.append("""
                    Name        : %s
                    Status      : %s
                    Capacity    : %.1f%%
                    Time Left   : %s
                    Voltage     : %.2f V
                    Amperage    : %.0f mA
                    Power Use   : %.2f W
                    Cycle Count : %d
                    Chemistry   : %s
                    ------------------------------
                    """.formatted(
                    battery.getName(),
                    status,
                    capacity,
                    timeLeft,
                    voltage,
                    amperage,
                    wattage,
                    Math.max(cycleCount, 0),
                    battery.getChemistry()
            ));
        }

        sb.append("==============================");
        return sb.toString();
    }



    /**
     * Export battery information as JSON.
     * @return JSON string with battery details
     */
    @Override
    public String toJson() {
        if (batteries.isEmpty()) {
            return """
                    {
                      "batteryDetected": false
                    }""";
        }



        StringBuilder sb = new StringBuilder("[\n");

        for (int i = 0; i < batteries.size(); i++) {
            PowerSource battery = batteries.get(i);
            battery.updateAttributes();

            sb.append("""
                      {
                        "name": "%s",
                        "charging": %b,
                        "discharging": %b,
                        "capacityPercent": %.1f,
                        "voltage": %.2f,
                        "amperageMa": %.0f,
                        "powerUsageWatt": %.2f,
                        "cycleCount": %d,
                        "chemistry": "%s"
                      }""".formatted(
                    battery.getName(),
                    battery.isCharging(),
                    battery.isDischarging(),
                    battery.getRemainingCapacityPercent() * 100,
                    battery.getVoltage(),
                    battery.getAmperage(),
                    battery.getPowerUsageRate(),
                    Math.max(battery.getCycleCount(), 0),
                    battery.getChemistry()
            ));



            if (i < batteries.size() - 1) sb.append(",");
            sb.append("\n");
        }

        sb.append("]");
        return sb.toString();
    }

    // Helper method doesn't to format time in hours and minutes
    // This uses formula from OSHI's PowerSource to convert seconds to a human-readable format
    private String formatTime(long seconds) {
        long hours   = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        return String.format("%dh %02dm", hours, minutes);
    }
}
