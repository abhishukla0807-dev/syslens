
/**
 * CollectorRegistry
 * Registry that holds references to all available collectors
 * (CPU, Memory, Disk, Network, etc.) and provides lookup services.
 */



package com.aurexiris.syslens.core;

import java.util.ArrayList;
import java.util.List;

public class CollectorRegistry {

    private final List<InfoCollector> collectors = new ArrayList<>();

    public void register(InfoCollector collector) {
        collectors.add(collector);
    }

    public List<InfoCollector> getAll() {
        return collectors;
    }

    public InfoCollector getByName(String name) {
        return collectors.stream()
                .filter(c -> c.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }
}