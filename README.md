
# SysLens — System Information & Monitoring Tool

SysLens is a lightweight **Java-based system monitoring utility** built with IntelliJ IDEA.  
It collects and reports detailed information about your system’s hardware, operating system, sensors, and runtime environment, with support for multiple output formats (Plain Text, JSON, HTML, Table).

---

## Features

- CPU, Memory, Disk, GPU, Battery, Network monitoring  
- Sensor telemetry (temperature, fans, voltage) via jSensors + OSHI fallback  
- OS & JVM runtime details  
- Process monitoring and uptime tracking  
- Live monitoring mode with configurable refresh an interval  
- Multiple output formatters: PlainText, JSON, HTML, Table  
- Export reports to file or clipboard  

---

## Project Setup in IntelliJ IDEA

### Step 1: Clone the Repository
```bash
git clone https://github.com/abhishukla0807-dev/syslens.git
cd syslens
```

### Step 2: Open in IntelliJ IDEA
1. Launch IntelliJ IDEA.
2. Click on **Open** and select the cloned `syslens` directory.
3. Wait for IntelliJ to index the project and resolve dependencies.

### Step 3: Configure SDK
1. Go to **File → Project Structure → Project**.
2. Set the Project SDK to **Java 17+**.
3. Set the Project language level to **17**.
4. Click **Apply** and **OK**.

### Step 4: Build and Run
1. Open `Main.java` located in `src/main/java/com/syslens/`.
2. Click the green **Run** button or press `Shift + F10` to execute the application.

### Step 5: Build Artifact
1. Go to **File → Project Structure → Artifacts**.
2. Add a new artifact: `+ → JAR → From modules with dependencies`.
3. Select `Main` as the main class.
4. Click **Apply** and **OK**.

### Step 6: Build the JAR
1. Go to **Build → Build Artifacts → Build**.
2. The JAR file will be generated in the `out/artifacts/` directory:
   ```
   out/artifacts/SysLens/SysLens.jar
   ```

### Step 7: Run the JAR
```bash
cd out/artifacts/SysLens
java -jar SysLens.jar
```

---

## Module Flags

- `--os`   → OS information
- `--cpu`  → CPU information
- `--ram`  → Memory information
- `--disk` → Disk information
- `--net`  → Network information
- `--bat`  → Battery information
- `--gpu`  → GPU information
- `--proc` → Process information

---

## Format Flags

- `--json`  → Output as JSON
- `--table` → Output as table
- `--html`  → Output as HTML

---

## Monitoring Mode

- `--monitor` → Run live monitor
- `--interval <seconds>` → Refresh interval

---

## Troubleshooting / Known Issues

- On Windows, OSHI may log:
  ```
  COM exception querying MSAcpi_ThermalZoneTemperature
  ```
  This means the WMI class isn’t available.
    - Run IntelliJ as Administrator for more sensor access.
    - Or run **LibreHardwareMonitor** in the background for richer telemetry.
    - This error is harmless — SysLens will continue to work using fallback methods.

- **Sensor data not visible in output**
    - In some systems, CPU temperature, voltage, or fan speed may show as `N/A (not supported)`.
    - This happens when hardware/firmware does not expose sensors or WMI is disabled.
    - To fix: enable WMI provider in LibreHardwareMonitor (`Options → WMI → Enable`).
    - Even with jSensors + OSHI, some laptops may still restrict sensor access.

---

## License
This project is licensed under the **MIT License**.

---

## Acknowledgements
- [OSHI](https://github.com/oshi/oshi) — Java system information library
- jSensors (github.com in Bing) [(bing.com in Bing)](https://www.bing.com/search?q="https%3A%2F%2Fwww.bing.com%2Fsearch%3Fq%3D%2522https%253A%252F%252Fgithub.com%252Fprofesorfalken%252FJSensors%2522") — Hardware sensor access
- [IntelliJ IDEA](https://www.jetbrains.com/idea/) — Development environment
```

