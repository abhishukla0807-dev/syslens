

package com.aurexiris.syslens.util;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Logger {

    public enum Level {
        INFO, WARN, ERROR, DEBUG
    }

    private static final String LOG_DIR  = "syslens-logs";
    private static final String LOG_FILE = "syslens.log";
    private static       boolean fileLoggingEnabled = false;
    private static       boolean debugEnabled       = false;

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // ── config ───────────────────────────────────────────────
    public static void enableFileLogging() {
        fileLoggingEnabled = true;
        try {
            Files.createDirectories(Paths.get(LOG_DIR));
        } catch (IOException e) {
            System.err.println("Logger: could not create log dir");
        }
    }

    public static void enableDebug() {
        debugEnabled = true;
    }

    // ── log methods ──────────────────────────────────────────
    public static void info(String message) {
        log(Level.INFO, message);
    }

    public static void warn(String message) {
        log(Level.WARN, message);
    }

    public static void error(String message) {
        log(Level.ERROR, message);
    }

    public static void debug(String message) {
        if (debugEnabled) log(Level.DEBUG, message);
    }

    public static void error(String message, Exception e) {
        log(Level.ERROR, message + " → " + e.getMessage());
    }

    // ── core ─────────────────────────────────────────────────
    private static void log(Level level, String message) {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        String entry     = String.format("[%s] [%-5s] %s",
                timestamp, level.name(), message);

        // console
        if (level == Level.ERROR || level == Level.WARN) {
            System.err.println(entry);
        } else {
            System.out.println(entry);
        }

        // file
        if (fileLoggingEnabled) {
            writeToFile(entry);
        }
    }

    private static void writeToFile(String entry) {
        Path logPath = Paths.get(LOG_DIR, LOG_FILE);
        try (PrintWriter pw = new PrintWriter(
                new FileWriter(logPath.toFile(), true))) {
            pw.println(entry);
        } catch (IOException e) {
            System.err.println("Logger: failed to write log file");
        }
    }
}