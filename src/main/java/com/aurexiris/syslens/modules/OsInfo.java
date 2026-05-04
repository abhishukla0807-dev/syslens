
/**
 * OsInfo
 * Module to collect operating system details such as name,
 * version, architecture, and vendor.
 */


package com.aurexiris.syslens.modules;

import com.aurexiris.syslens.core.InfoCollector;
import oshi.SystemInfo;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.software.os.OperatingSystem;

public class OsInfo implements InfoCollector {

    private final SystemInfo si = new SystemInfo();
    private final OperatingSystem os = si.getOperatingSystem();

    @Override
    public String getName() {
        return "OS Info";
    }

    @Override
    public String collect() {
        return """
                ==============================
                        OS INFORMATION
                ==============================
                OS          : %s
                Version     : %s
                Arch        : %s
                Manufacturer: %s
                Processes   : %d
                ==============================
                """.formatted(
                os.getFamily(),
                os.getVersionInfo().getVersion(),
                System.getProperty("os.arch"),
                os.getManufacturer(),
                os.getProcessCount()
        );
    }

    @Override
    public String toJson() {
        String formatted = """
                {
                  "family": "%s",
                  "version": "%s",
                  "arch": "%s",
                  "manufacturer": "%s",
                  "processCount": %d
                }""".formatted(
                os.getFamily(),
                os.getVersionInfo().getVersion(),
                System.getProperty("os.arch"),
                os.getManufacturer(),
                os.getProcessCount()
        );
        return formatted;
    }
}