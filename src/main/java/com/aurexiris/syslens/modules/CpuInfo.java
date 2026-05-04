
/**
 * CpuInfo
 * Module to collect CPU statistics including cores, threads,
 * usage percentage, and processor model.
 */

package com.aurexiris.syslens.modules;

import com.aurexiris.syslens.core.InfoCollector;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;

public class CpuInfo implements InfoCollector {

    private final CentralProcessor cpu =
            new SystemInfo().getHardware().getProcessor();

    @Override
    public String getName() {
        return "CPU Info";
    }

    @Override
    public String collect() {
        // correct
        long[] prevTicks = cpu.getSystemCpuLoadTicks();

// small delay for accurate reading
        try { Thread.sleep(1000); } catch (InterruptedException ignored) {}

        double cpuLoad = cpu.getSystemCpuLoadBetweenTicks(prevTicks) * 100;

        return """
                ==============================
                        CPU INFORMATION
                ==============================
                Name        : %s
                Physical    : %d cores
                Logical     : %d threads
                Base Freq   : %.2f GHz
                CPU Load    : %.1f%%
                ==============================
                """.formatted(
                cpu.getProcessorIdentifier().getName(),
                cpu.getPhysicalProcessorCount(),
                cpu.getLogicalProcessorCount(),
                cpu.getProcessorIdentifier().getVendorFreq() / 1_000_000_000.0,
                cpuLoad
        );
    }

    @Override
    public String toJson() {
        return """
                {
                  "name": "%s",
                  "physicalCores": %d,
                  "logicalThreads": %d,
                  "baseFreqGHz": %.2f
                }""".formatted(
                cpu.getProcessorIdentifier().getName(),
                cpu.getPhysicalProcessorCount(),
                cpu.getLogicalProcessorCount(),
                cpu.getProcessorIdentifier().getVendorFreq() / 1_000_000_000.0
        );
    }
}