
package com.aurexiris.syslens.util;

public class PlatformDetector {

    public enum OS {
        WINDOWS, LINUX, MAC, UNKNOWN
    }

    private static final OS CURRENT = detect();

    // ── detect ───────────────────────────────────────────────
    private static OS detect() {
        String name = System.getProperty("os.name")
                .toLowerCase();
        if (name.contains("win"))   return OS.WINDOWS;
        if (name.contains("mac"))   return OS.MAC;
        if (name.contains("nix")
                || name.contains("nux")
                || name.contains("aix"))   return OS.LINUX;
        return OS.UNKNOWN;
    }

    // ── getters ──────────────────────────────────────────────
    public static OS getOS()         { return CURRENT;              }
    public static boolean isWindows(){ return CURRENT == OS.WINDOWS; }
    public static boolean isLinux()  { return CURRENT == OS.LINUX;   }
    public static boolean isMac()    { return CURRENT == OS.MAC;     }

    public static String getName() {
        return System.getProperty("os.name");
    }

    public static String getArch() {
        return System.getProperty("os.arch");
    }

    public static String getVersion() {
        return System.getProperty("os.version");
    }

    public static String getSeparator() {
        return System.getProperty("file.separator");
    }

    // ── line separator per OS ────────────────────────────────
    public static String getLineSeparator() {
        return switch (CURRENT) {
            case WINDOWS -> "\r\n";
            case LINUX   -> "\n";
            case MAC     -> "\n";
            default      -> System.lineSeparator();
        };
    }

    public static String summary() {
        return String.format("OS: %s | Arch: %s | Version: %s",
                getName(), getArch(), getVersion());
    }
}