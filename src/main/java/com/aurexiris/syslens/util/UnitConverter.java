package com.aurexiris.syslens.util;

public class UnitConverter {

    // ── Bytes ─────────────────────────────────────────────────
    public static String bytesToHuman(long bytes) {
        if (bytes <= 0)            return "0 B";
        if (bytes >= 1_073_741_824L)
            return String.format("%.2f GB", bytes / 1_073_741_824.0);
        if (bytes >= 1_048_576L)
            return String.format("%.2f MB", bytes / 1_048_576.0);
        if (bytes >= 1_024L)
            return String.format("%.2f KB", bytes / 1_024.0);
        return bytes + " B";
    }

    // ── Frequency ────────────────────────────────────────────
    public static String hzToHuman(long hz) {
        if (hz <= 0)               return "N/A";
        if (hz >= 1_000_000_000L)
            return String.format("%.2f GHz", hz / 1_000_000_000.0);
        if (hz >= 1_000_000L)
            return String.format("%.2f MHz", hz / 1_000_000.0);
        if (hz >= 1_000L)
            return String.format("%.2f KHz", hz / 1_000.0);
        return hz + " Hz";
    }

    // ── Speed ────────────────────────────────────────────────
    public static String bpsToHuman(long bps) {
        if (bps <= 0)              return "N/A";
        if (bps >= 1_000_000_000L)
            return String.format("%.1f Gbps", bps / 1_000_000_000.0);
        if (bps >= 1_000_000L)
            return String.format("%.1f Mbps", bps / 1_000_000.0);
        if (bps >= 1_000L)
            return String.format("%.1f Kbps", bps / 1_000.0);
        return bps + " bps";
    }

    // ── Temperature ──────────────────────────────────────────
    public static String celsiusToHuman(double celsius) {
        if (celsius <= 0) return "N/A";
        double fahrenheit = (celsius * 9 / 5) + 32;
        return String.format("%.1f°C / %.1f°F", celsius, fahrenheit);
    }

    // ── Time ─────────────────────────────────────────────────
    public static String secondsToHuman(long seconds) {
        if (seconds <= 0) return "0s";
        long days    = seconds / 86400;
        long hours   = (seconds % 86400) / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs    = seconds % 60;

        if (days > 0)
            return String.format("%dd %dh %02dm %02ds",
                    days, hours, minutes, secs);
        if (hours > 0)
            return String.format("%dh %02dm %02ds",
                    hours, minutes, secs);
        return String.format("%dm %02ds", minutes, secs);
    }

    // ── Percentage ───────────────────────────────────────────
    public static String toPercent(long used, long total) {
        if (total <= 0) return "N/A";
        return String.format("%.1f%%", (used * 100.0) / total);
    }

    public static double toPercentDouble(long used, long total) {
        if (total <= 0) return 0.0;
        return (used * 100.0) / total;
    }
}