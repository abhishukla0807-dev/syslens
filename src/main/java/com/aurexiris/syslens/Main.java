package com.aurexiris.syslens;

import com.aurexiris.syslens.core.CollectorRegistry;
import com.aurexiris.syslens.core.InfoCollector;
import com.aurexiris.syslens.core.SystemSnapshot;
import com.aurexiris.syslens.modules.CpuInfo;
import com.aurexiris.syslens.modules.DiskInfo;
import com.aurexiris.syslens.modules.MemoryInfo;
import com.aurexiris.syslens.modules.OsInfo;

public class Main {

    public static void main(String[] args) {

        // Registry->register all modules
        CollectorRegistry registry = new CollectorRegistry();
        registry.register(new OsInfo());
        registry.register(new CpuInfo());
        registry.register(new MemoryInfo());
        registry.register(new DiskInfo());
        // more modules added here later...

        // Snapshot->collect all data
        SystemSnapshot snapshot = new SystemSnapshot();
        for (InfoCollector collector : registry.getAll()) {
            snapshot.add(
                    collector.getName(),
                    collector.collect(),
                    collector.toJson()
            );
        }

        // Output—> print to terminal
        System.out.println(snapshot.getAllText());
    }
}