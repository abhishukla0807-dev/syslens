# SysLens — System Information & Monitoring Tool

SysLens is a lightweight, cross-platform **Java-based system monitoring utility** built with Java 21, OSHI, and Maven.  
It collects and reports detailed metrics about your system's hardware, operating system, sensors, and runtime environment, with support for multiple output formats (Plain Text, JSON, HTML, Table) and live monitoring with alerting.

---

## Features

- **Hardware & System Metrics**: CPU, Memory, Disk, GPU, Battery, Network, OS, and JVM runtime details.
- **Hardware Sensor Telemetry**: CPU temperature (in both Celsius and Fahrenheit) and CPU voltage.
- **Process & Uptime Tracking**: Top active processes and system uptime tracking.
- **Live Monitoring Mode**: Continuous terminal dashboard with configurable refresh interval.
- **Health Alerting Engine**: Proactive warnings when CPU, RAM, Disk, or Temperature exceed configurable safety thresholds.
- **Multiple Output Formatters**: PlainText, JSON, HTML, and Table formats.
- **Export & Clipboard Integration**: Save reports to file (`syslens-reports/`) or copy directly to clipboard.

---

## One-Click Windows Executable (.exe)

SysLens includes native Windows `.exe` launchers for instant one-click execution:

- **Project Root Launcher**: Double-click [`SysLens.exe`](SysLens.exe) in the repository root.
- **Bundled Release**: Double-click [`release/syslens/SysLens.exe`](release/syslens/SysLens.exe).
- **Portable Standalone Package**: Double-click [`release/standalone/SysLens/SysLens.exe`](release/standalone/SysLens/SysLens.exe) (fully self-contained with bundled lightweight JRE, requires no Java installed on the target machine).

### Interactive Double-Click Experience
When double-clicked from Windows Explorer, SysLens:
1. Automatically initializes UTF-8 console output and ANSI styling.
2. Displays the banner and complete system snapshot report.
3. Keeps the console window open with an interactive menu:
   - `[1]` Refresh system snapshot
   - `[2]` Launch Real-Time Live Monitor Dashboard
   - `[3]` Export report to HTML (`syslens-reports/`)
   - `[4]` Export report to JSON (`syslens-reports/`)
   - `[Q]` Exit cleanly

### Rebuilding the Executable
Run or double-click [`build-exe.bat`](build-exe.bat) to recompile the fat JAR, regenerate the native Windows launchers, and package the standalone distribution in one step.

---

## Quick Start (Maven CLI)

### Prerequisites
- **Java Development Kit (JDK) 21+**
- **Apache Maven 3.8+**

### 1. Clone & Build
```bash
git clone https://github.com/abhishukla0807-dev/syslens.git
cd syslens

# Build the executable fat JAR
mvn clean package
```

### 2. Run SysLens
```bash
# Run all modules with default formatted output
java -jar target/syslens.jar

# Run specific hardware sensors module
java -jar target/syslens.jar --sensor

# Run live monitoring mode
java -jar target/syslens.jar --monitor --interval 3
```

---

## Project Setup in IntelliJ IDEA

1. Launch IntelliJ IDEA and select **Open** on the cloned `syslens` directory.
2. Configure Project SDK:
   - Go to **File → Project Structure → Project**.
   - Set the SDK to **Java 21**.
   - Set the Language level to **21 - Preview** or **21**.
3. Open `Main.java` located at:
   ```
   src/main/java/com/aurexiris/syslens/Main.java
   ```
4. Click the green **Run** button or press `Shift + F10`.

---

## Command-Line Usage

### Module Flags (show specific info only)
- `--os`     → Operating system information
- `--cpu`    → Processor details and CPU load
- `--ram`    → Memory (physical & swap) usage
- `--disk`   → Disk partitions, usage, and read/write
- `--net`    → Network interfaces and IP addresses
- `--bat`    → Battery health, charge %, and status
- `--gpu`    → Graphics card information and VRAM
- `--proc`   → Top system processes
- `--sensor` → Hardware sensors (CPU temp, voltage) *(aliases: `--sensors`, `--temp`)*

### Format Flags
- `--json`   → Structured JSON output
- `--table`  → Tabular ASCII report with borders
- `--html`   → Styled HTML report with dark mode

### Export & Clipboard Flags
- `--export` → Save the generated report to disk (in `syslens-reports/`)
- `--clip`   → Copy the report directly to the system clipboard

### Live Monitor Flags
- `--monitor`            → Launch real-time live monitoring dashboard
- `--interval <seconds>` → Refresh interval in seconds (default: 5s)

### General Flags
- `--config`      → View current configuration settings
- `--help`, `-h`  → Display CLI usage guide

### Examples
```bash
# Export full JSON report to file
java -jar target/syslens.jar --json --export

# Output HTML report to file
java -jar target/syslens.jar --html --export

# View hardware sensors only
java -jar target/syslens.jar --sensor

# Copy CPU & temperature report to clipboard
java -jar target/syslens.jar --sensor --clip

# Run real-time monitor refreshed every 2 seconds
java -jar target/syslens.jar --monitor --interval 2
```

---

## Hardware Sensors & Temperature Implementation

SysLens provides hardware sensor monitoring via the dedicated `SensorInfo` module (`com.aurexiris.syslens.modules.SensorInfo`), which implements the unified `InfoCollector` interface.

### Output Format
```text
==============================
       HARDWARE SENSORS
==============================
CPU Temp    : 54.0°C (129.2°F)
CPU Voltage : 1.15 V
==============================
```

### How It Works Under the Hood
SysLens combines **LibreHardwareMonitor** (via `jLibreHardwareMonitor`) and **OSHI**:
1. **Windows (LibreHardwareMonitor)**:
   - SysLens bundles `jLibreHardwareMonitor`, which embeds `LibreHardwareMonitorLib.dll`.
   - When run with **Administrator privileges**, it interfaces with low-level hardware drivers (`WinRing0`) to query CPU MSR registers (for dynamic CPU VCore voltage and core temperatures).
2. **Cross-Platform Fallback (OSHI)**:
   - If running on Linux or macOS, or if LibreHardwareMonitor is unavailable, SysLens seamlessly falls back to OSHI (`oshi.hardware.Sensors`).
   - On Linux, it reads kernel sysfs (`/sys/class/thermal/` and `/sys/class/hwmon/`).
   - On macOS, it queries the Apple System Management Controller (SMC).

### Fallback & Graceful Degradation
- If a sensor or motherboard vendor does not expose metrics, or if the terminal is not running as Administrator, SysLens safely displays `N/A (not supported / run as Admin)` rather than crashing or showing false `0.0°C` readings.
- Third-party COM/WMI internal debug exceptions are captured and suppressed via `logback.xml` to keep terminal output clean and readable.

### Proactive Thermal Alerts
Temperature thresholds are integrated with SysLens's `AlertEngine`. In `src/main/resources/config.properties`:
```properties
alert.temp.warn     = 75.0
alert.temp.critical = 85.0
```
During live monitoring (`--monitor`), if CPU temperature reaches or exceeds these limits, warning or critical alerts are automatically triggered.

---

## Troubleshooting / Known Issues

- **Sensor data shows as `N/A (not supported / run as Admin)` on Windows**:
  - Low-level hardware sensors (CPU temperature, CPU VCore) require kernel-level driver access (`WinRing0`).
  - **Solution**: Open your terminal, PowerShell, or command prompt as **Administrator**, then run:
    ```bash
    java -jar target/syslens.jar --sensor
    ```
  - Alternatively, if you run the standalone [LibreHardwareMonitor GUI](https://github.com/LibreHardwareMonitor/LibreHardwareMonitor/releases) with WMI enabled (`Options → WMI → Enable`), SysLens will also automatically read its WMI bridge.

---

## License
This project is licensed under the **MIT License** — see the [LICENSE](LICENSE) file for details.

---

## Acknowledgements
- [LibreHardwareMonitor](https://github.com/LibreHardwareMonitor/LibreHardwareMonitor) & [jLibreHardwareMonitor](https://github.com/pandalxb/jLibreHardwareMonitor) — Hardware monitoring library for Windows sensors (voltage, temperature)
- [OSHI](https://github.com/oshi/oshi) — Native Operating System and Hardware Information library for Java
- [Gson](https://github.com/google/gson) — JSON serialization
- [Jansi](https://github.com/fusesource/jansi) — ANSI terminal color formatting
- [Logback](https://logback.qos.ch/) — Reliable logging framework
