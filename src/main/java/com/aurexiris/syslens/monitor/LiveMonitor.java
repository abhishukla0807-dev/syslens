/**
 * LiveMonitor.java
 *
 * Author: Aman
 * Version: 1.0.0
 * Last Updated: 2026-05-05
 *
 * Description:
 *   LiveMonitor is the real-time monitoring engine of SysLens.
 *   It continuously collects system metrics (CPU, RAM, Disk,
 *   Network, Battery) at configurable intervals, displays them
 *   in a formatted output, and triggers alerts when thresholds
 *   are exceeded.
 *
 * Purpose:
 *   - Provide live system monitoring with configurable refresh intervals.
 *   - Integrate multiple InfoCollector modules into a unified snapshot.
 *   - Support alerting via AlertEngine for proactive system health checks.
 *
 * Notes for Contributors:
 *   - a Refresh interval is configurable via AppConfig ("monitor.refresh.seconds").
 *   - Extend by registering additional InfoCollector modules (e.g., GPU, Uptime).
 *   - Alerts are checked after each snapshot using AlertEngine.
 *   - Graceful shutdown is supported via Ctrl+C.
 */

package com.aurexiris.syslens.monitor;

import com.aurexiris.syslens.config.AppConfig;
import com.aurexiris.syslens.core.CollectorRegistry;
import com.aurexiris.syslens.core.InfoCollector;
import com.aurexiris.syslens.core.SystemSnapshot;
import com.aurexiris.syslens.modules.CpuInfo;
import com.aurexiris.syslens.modules.MemoryInfo;
import com.aurexiris.syslens.modules.DiskInfo;
import com.aurexiris.syslens.modules.NetworkInfo;
import com.aurexiris.syslens.modules.BatteryInfo;
import com.aurexiris.syslens.modules.SensorInfo;
import com.aurexiris.syslens.output.PlainTextFormatter;

public class LiveMonitor {

    private static final int DEFAULT_INTERVAL = 5; // seconds

    private final int         intervalSeconds;
    private final AlertEngine alertEngine;
    private       boolean     running = true;

    /**
     * Default constructor: uses DEFAULT_INTERVAL.
     */
    public LiveMonitor() {
        this(DEFAULT_INTERVAL);
    }

    /**
     * Constructor: initializes a refresh interval from AppConfig.
     * @param intervalSeconds default interval (overridden by config)
     */
    public LiveMonitor(int intervalSeconds) {
        AppConfig config = AppConfig.getInstance();
        this.intervalSeconds = config.getInt("monitor.refresh.seconds", intervalSeconds);
        this.alertEngine     = new AlertEngine();
    }

    /**
     * Start the live monitoring loop.
     * Collects metrics, prints formatted output, and checks alerts.
     */
    public void start() {
        printMonitorBanner();

        // graceful shutdown on Ctrl+C
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            running = false;
            System.out.println("\n\n⛔ SysLens Monitor stopped.");
        }));

        while (running) {
            try {
                clearScreen();
                printHeader();

                // collect live modules only
                CollectorRegistry registry = new CollectorRegistry();
                registry.register(new CpuInfo());
                registry.register(new MemoryInfo());
                registry.register(new DiskInfo());
                registry.register(new NetworkInfo());
                registry.register(new BatteryInfo());
                registry.register(new SensorInfo());

                SystemSnapshot snapshot = new SystemSnapshot();
                for (InfoCollector collector : registry.getAll()) {
                    snapshot.add(
                            collector.getName(),
                            collector.collect(),
                            collector.toJson()
                    );
                }

                // print output
                System.out.println(new PlainTextFormatter().format(snapshot));

                // check alerts
                alertEngine.check(snapshot);

                // countdown until next refresh
                countdown(intervalSeconds);

            } catch (InterruptedException e) {
                running = false;
                Thread.currentThread().interrupt();
            }
        }
    }

    // ── helpers ──────────────────────────────────────────────

    // Clear screen for Windows and Unix systems
    private void clearScreen() {
        try {
            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                new ProcessBuilder("cmd", "/c", "cls")
                        .inheritIO().start().waitFor();
            } else {
                System.out.print("\033[H\033[2J");
                System.out.flush();
            }
        } catch (Exception ignored) {
            System.out.println("\n".repeat(50));
        }
    }

    // Countdown timer between refreshes
    private void countdown(int seconds) throws InterruptedException {
        for (int i = seconds; i > 0; i--) {
            System.out.printf(
                    "\r⏱  Refreshing in %d second(s)... (Press Ctrl+C to stop)", i);
            Thread.sleep(1000);
        }
    }

    // Print header with current time and refresh interval
    private void printHeader() {
        String time = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));

        System.out.println("""
                ╔══════════════════════════════════════╗
                ║     SYSLENS — LIVE MONITOR           ║
                ║     Refresh: every %2ds               ║
                ╚══════════════════════════════════════╝
                  🕐 %s
                """.formatted(intervalSeconds, time));
    }

    // Print startup banner
    private void printMonitorBanner() {
        System.out.println("""
                
                ⚡ Starting SysLens Live Monitor...
                   Press Ctrl+C to stop.
                """);

        try { Thread.sleep(1000); }
        catch (InterruptedException ignored) {}
    }
}
