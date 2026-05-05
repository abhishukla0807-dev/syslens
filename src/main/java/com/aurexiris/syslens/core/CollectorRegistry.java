/**
 * CollectorRegistry.java
 *
 * Description:
 *   CollectorRegistry is a lightweight container that manages all
 *   registered InfoCollector modules like CPU, Memory, Disk, Network, etc.
 *   It provides methods to register new collectors, retrieve all
 *   collectors, and look up a collector by name.
 *
 * Purpose:
 *   - Centralize the management of system information collectors.
 *   - Allow contributors to easily extend SysLens by adding new
 *     InfoCollector implementations e.g., GPUInfo, BatteryInfo.
 *   - Provide lookup services for modules during snapshot creation.
 *
 * Notes for Contributors:
 *   - Use register() to add new collectors.
 *   - getAll() returns the full list of registered collectors.
 *   - getByName() performs a case-insensitive lookup by collector name.
 *   - Keep this class simple and focused on registry responsibilities.
 */

package com.aurexiris.syslens.core;

import java.util.ArrayList;
import java.util.List;

public class CollectorRegistry {

    // Internal list holding all registered collectors
    private final List<InfoCollector> collectors = new ArrayList<>();

    /**
     * Register a new InfoCollector instance.
     * @param collector the collector to add
     */
    public void register(InfoCollector collector) {
        collectors.add(collector);
    }



    /**
     * Retrieve all registered collectors.
     * @return list of InfoCollector instances
     */
    public List<InfoCollector> getAll() {
        return collectors;
    }


}
