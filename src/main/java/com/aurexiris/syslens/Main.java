/**
 * Main.java
 *
 * Author: Abhishek Shukla
 * Version: 1.0.0
 * Last Updated: 2026-05-05
 *
 * Description:
 *   Main is the entry point for the SysLens application. It handles
 *   command-line arguments, initializes configuration and logging,
 *   registers InfoCollector modules, collects system snapshots,
 *   formats output, and manages export/clipboard operations.
 *
 * Purpose:
 *   - Provide a unified CLI interface for SysLens.
 *   - Support flexible module selection via flags (--cpu, --ram, etc.).
 *   - Enable multiple output formats (PlainText, JSON, HTML, Table).
 *   - Allow exporting reports to file or clipboard.
 *   - Run live monitoring mode with configurable refresh intervals.
 *
 * Notes for Contributors:
 *   - Extend by adding new module flags and registering corresponding
 *     InfoCollector implementations.
 *   - Keep help text updated when new modules or flags are introduced.
 *   - Logging is controlled via AppConfig (log.enabled, log. Debug).
 *   - LiveMonitor supports interval configuration via an --interval flag.
 */

package com.aurexiris.syslens;

import com.aurexiris.syslens.config.AppConfig;
import com.aurexiris.syslens.core.CollectorRegistry;
import com.aurexiris.syslens.core.InfoCollector;
import com.aurexiris.syslens.core.SystemSnapshot;
import com.aurexiris.syslens.export.ClipboardExporter;
import com.aurexiris.syslens.export.FileExporter;
import com.aurexiris.syslens.modules.*;
import com.aurexiris.syslens.monitor.LiveMonitor;
import com.aurexiris.syslens.output.*;
import com.aurexiris.syslens.util.Logger;

import java.util.Arrays;
import java.util.List;

public class Main {

    /**
     * Entry point for SysLens.
     * Parses command-line arguments and executes appropriate actions.
     */
    public static void main(String[] args) {
        List<String> argList = Arrays.asList(args);

        // ── config & logging ────────────────────────────────
        AppConfig config = AppConfig.getInstance();

        if (config.getBoolean("log.enabled", false)) {
            Logger.enableFileLogging();
        }
        if (config.getBoolean("log.debug", false)) {
            Logger.enableDebug();
        }

        // Print config if requested
        if (argList.contains("--config")) {
            config.printAll();
            return;
        }

        // ── help ─────────────────────────────────────────────
        if (argList.contains("--help") || argList.contains("-h")) {
            printHelp();
            return;
        }

        // ── live monitor ─────────────────────────────────────
        if (argList.contains("--monitor")) {
            int interval = 5; // default 5 seconds
            int index = argList.indexOf("--interval");
            if (index != -1 && index < argList.size() - 1) {
                try {
                    interval = Integer.parseInt(argList.get(index + 1));
                } catch (NumberFormatException e) {
                    System.err.println("Invalid interval value. Using default 5 seconds.");
                }
            }
            new LiveMonitor(interval).start();
            return;
        }

        // ── banner ───────────────────────────────────────────
        printBanner();

        // ── registry ─────────────────────────────────────────
        CollectorRegistry registry = new CollectorRegistry();

        // Filter by module flag or load all
        if (argList.contains("--os"))
            registry.register(new OsInfo());
        else if (argList.contains("--cpu"))
            registry.register(new CpuInfo());
        else if (argList.contains("--ram"))
            registry.register(new MemoryInfo());
        else if (argList.contains("--disk"))
            registry.register(new DiskInfo());
        else if (argList.contains("--net"))
            registry.register(new NetworkInfo());
        else if (argList.contains("--bat"))
            registry.register(new BatteryInfo());
        else if (argList.contains("--gpu"))
            registry.register(new GpuInfo());
        else if (argList.contains("--proc"))
            registry.register(new ProcessInfo());
        else {
            // Default → all modules
            registry.register(new OsInfo());
            registry.register(new UptimeInfo());
            registry.register(new CpuInfo());
            registry.register(new MemoryInfo());
            registry.register(new DiskInfo());
            registry.register(new GpuInfo());

            registry.register(new BatteryInfo());
            registry.register(new ProcessInfo());

            registry.register(new JavaRuntimeInfo());

            registry.register(new NetworkInfo());
        }

        // ── collect ──────────────────────────────────────────
        SystemSnapshot snapshot = new SystemSnapshot();
        for (InfoCollector collector : registry.getAll()) {
            snapshot.add(
                    collector.getName(),
                    collector.collect(),
                    collector.toJson());
        }

        // ── format ───────────────────────────────────────────
        OutputFormatter formatter;
        if (argList.contains("--json"))
            formatter = new JsonFormatter();
        else if (argList.contains("--table"))
            formatter = new TableFormatter();
        else if (argList.contains("--html"))
            formatter = new HtmlFormatter();
        else
            formatter = new PlainTextFormatter();

        String output = formatter.format(snapshot);

        // ── print to terminal ────────────────────────────────
        System.out.println(output);

        // ── export to file ───────────────────────────────────
        if (argList.contains("--export")) {
            FileExporter exporter = new FileExporter();
            if (argList.contains("--json"))
                exporter.export(snapshot, "json");
            else if (argList.contains("--html"))
                exporter.export(snapshot, "html");
            else
                exporter.export(snapshot, "text");
        }

        // ── copy to clipboard ────────────────────────────────
        if (argList.contains("--clip")) {
            new ClipboardExporter().export(snapshot);
        }
    }

    // ── banner ───────────────────────────────────────────────
    private static void printBanner() {
        System.out.println("""

                ███████╗██╗   ██╗███████╗██╗     ███████╗███╗   ██╗███████╗
                ██╔════╝╚██╗ ██╔╝██╔════╝██║     ██╔════╝████╗  ██║██╔════╝
                ███████╗ ╚████╔╝ ███████╗██║     █████╗  ██╔██╗ ██║███████╗
                ╚════██║  ╚██╔╝  ╚════██║██║     ██╔══╝  ██║╚██╗██║╚════██║
                ███████║   ██║   ███████║███████╗███████╗██║ ╚████║███████║
                ╚══════╝   ╚═╝   ╚══════╝╚══════╝╚═╝  ╚═══╝╚══════╝
                                  System Information Tool v1.0.0
                """);
    }

    // ── help ─────────────────────────────────────────────────
    private static void printHelp() {
        System.out.println("""

                SysLens — System Information Tool v1.0.0
                Usage: java -jar syslens.jar [options]

                MODULE FLAGS (show specific info only):
                  --os       OS information
                  --cpu      CPU information
                  --ram      Memory information
                  --disk     Disk information
                  --net      Network information
                  --bat      Battery information
                  --gpu      GPU information
                  --proc     Process information

                FORMAT FLAGS:
                  --json     Output as JSON
                  --table    Output as table
                  --html     Output as HTML

                EXPORT FLAGS:
                  --export   Save report to file
                  --clip     Copy report to clipboard

                MONITOR FLAGS:
                  --monitor  Run live monitor
                  --interval <seconds>  Refresh interval for monitor

                EXAMPLES:
                  java -jar syslens.jar
                  java -jar syslens.jar --cpu
                  java -jar syslens.jar --json --export
                  java -jar syslens.jar --html --export
                  java -jar syslens.jar --ram --clip
                  java -jar syslens.jar --monitor --interval 10
                """);
    }
}
