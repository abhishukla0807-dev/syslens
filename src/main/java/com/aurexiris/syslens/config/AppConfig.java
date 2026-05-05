/**
 * AppConfig.java
 * <p>
 * Description:
 *   AppConfig is the central configuration manager for SysLens.
 *   It loads settings from an external config.properties file (if present),
 *   or falls back to bundled defaults. It provides typed getters for
 *   configuration values and supports saving updated settings.
 *
 * Purpose:
 *   - Ensure SysLens is configurable without recompilation.
 *   - Provide a single source of truth for application settings.
 *   - Allow contributors to extend SysLens by adding new config keys
 *     for modules, alerts, logging, or export features.
 *
 * Notes for Contributors:
 *   - External config is read from "syslens-config/config.properties".
 *   - Defaults are defined in loadDefaults().
 *   - Use typed getters (getInt, getDouble, getBoolean) to avoid parsing errors.
 *   - Add new defaults in loadDefaults() and document them in README.
 */

package com.aurexiris.syslens.config;

import java.io.*;
import java.nio.file.*;
import java.util.Properties;

public class AppConfig {

    private static final String CONFIG_FILE = "config.properties";
    private static final String CONFIG_DIR  = "syslens-config";

    private final Properties props = new Properties();
    private static AppConfig instance;

    // Singleton pattern: ensures only one AppConfig instance exists
    private AppConfig() {
        load();
    }

    public static AppConfig getInstance() {
        if (instance == null) {
            instance = new AppConfig();
        }
        return instance;
    }

    // Load configuration from an external file or bundled resources
    private void load() {
        Path externalConfig = Paths.get(CONFIG_DIR, CONFIG_FILE);

        if (Files.exists(externalConfig)) {
            try (InputStream is = Files.newInputStream(externalConfig)) {
                props.load(is);
                System.out.println("Config loaded from: " + externalConfig);
            } catch (IOException e) {
                System.err.println("Failed to load external config: " + e.getMessage());
                loadDefaults();
            }
        } else {
            try (InputStream is = getClass().getClassLoader().getResourceAsStream(CONFIG_FILE)) {
                if (is != null) {
                    props.load(is);
                } else {
                    loadDefaults();
                }
            } catch (IOException e) {
                loadDefaults();
            }
        }
    }

    // Define default configuration values
    private void loadDefaults() {
        props.setProperty("app.version", "1.0.0");
        props.setProperty("app.name", "SysLens");

        props.setProperty("monitor.refresh.seconds", "5");
        props.setProperty("monitor.show.banner", "true");

        props.setProperty("alert.cpu.warn", "75.0");
        props.setProperty("alert.cpu.critical", "90.0");
        props.setProperty("alert.ram.warn", "75.0");
        props.setProperty("alert.ram.critical", "90.0");
        props.setProperty("alert.disk.warn", "80.0");
        props.setProperty("alert.disk.critical", "95.0");

        props.setProperty("export.dir", "syslens-reports");
        props.setProperty("export.default.format", "text");

        props.setProperty("log.enabled", "false");
        props.setProperty("log.dir", "syslens-logs");
        props.setProperty("log.debug", "false");

        props.setProperty("process.top.count", "10");

        props.setProperty("display.show.banner", "true");
        props.setProperty("display.color.enabled", "true");
    }

    // Typed getters for configuration values
    public String get(String key) {
        return props.getProperty(key, "");
    }

    public String get(String key, String defaultValue) {
        return props.getProperty(key, defaultValue);
    }

    public int getInt(String key, int defaultValue) {
        try {
            return Integer.parseInt(props.getProperty(key));
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public double getDouble(String key, double defaultValue) {
        try {
            return Double.parseDouble(props.getProperty(key));
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        String val = props.getProperty(key);
        if (val == null) return defaultValue;
        return Boolean.parseBoolean(val);
    }

    // Save current configuration to an external file
    public void save() {
        Path dir    = Paths.get(CONFIG_DIR);
        Path config = dir.resolve(CONFIG_FILE);

        try {
            Files.createDirectories(dir);
            try (OutputStream os = Files.newOutputStream(config)) {
                props.store(os, "SysLens Configuration");
                System.out.println("Config saved to: " + config);
            }
        } catch (IOException e) {
            System.err.println("Failed to save config: " + e.getMessage());
        }
    }

    // Update a configuration property in memory
    public void set(String key, String value) {
        props.setProperty(key, value);
    }

    // Print all configuration values to console
    public void printAll() {
        System.out.println("================================");
        System.out.println("        SYSLENS CONFIGURATION   ");
        System.out.println("================================");
        props.stringPropertyNames()
                .stream()
                .sorted()
                .forEach(key -> System.out.printf("%-35s = %s%n", key, props.getProperty(key)));
        System.out.println("================================");
    }
}
