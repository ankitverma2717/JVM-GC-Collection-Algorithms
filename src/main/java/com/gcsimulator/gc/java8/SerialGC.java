package com.gcsimulator.gc.java8;

import com.gcsimulator.gc.*;
import com.gcsimulator.model.jvm.JVMSimulator;
import com.gcsimulator.model.memory.*;
import com.gcsimulator.model.objects.SimulatedObject;

import java.util.*;

/**
 * Serial GC: Single-threaded stop-the-world collector.
 * Uses mark-sweep-compact for both young and old generations.
 */
public class SerialGC extends AbstractGarbageCollector {

    @Override
    public GCAlgorithm getAlgorithm() {
        return GCAlgorithm.SERIAL_GC;
    }

    @Override
    public boolean collect() {
        long startTime = System.currentTimeMillis();
        jvm.incrementGCs();

        log("Starting collection (Stop-The-World)");
        jvm.fireEvent(new JVMSimulator.JVMEvent(
                JVMSimulator.JVMEvent.EventType.GC_STARTED,
                "Serial GC started",
                null));

        // Phase 1: Minor GC (Young Generation)
        long bytesCollected = minorGC();

        // Phase 2: Major GC (Old Generation) if needed
        if (jvm.getHeap().getOldGen().getUtilization() >= oldGenThreshold) {
            bytesCollected += majorGC();
        }

        long pauseTime = System.currentTimeMillis() - startTime;
        statistics.recordCollection(pauseTime, bytesCollected);

        log(String.format("Collection completed in %dms, collected %d bytes", pauseTime, bytesCollected));
        jvm.fireEvent(new JVMSimulator.JVMEvent(
                JVMSimulator.JVMEvent.EventType.GC_COMPLETED,
                "Serial GC completed",
                pauseTime));

        return true;
    }

    /**
     * Minor GC: Collect young generation.
     */
    private long minorGC() {
        log("Phase 1: Minor GC (Young Generation)");

        YoungGeneration youngGen = jvm.getHeap().getYoungGen();
        MemoryRegion eden = youngGen.getEden();
        MemoryRegion fromSurvivor = youngGen.getFromSurvivor();
        MemoryRegion toSurvivor = youngGen.getToSurvivor();

        // Mark reachable objects
        Set<SimulatedObject> reachable = mark();

        long bytesCollected = 0;
        List<SimulatedObject> survivors = new ArrayList<>();

        // Process Eden
        for (Object obj : new ArrayList<>(eden.getObjects())) {
            if (obj instanceof SimulatedObject simObj) {
                if (reachable.contains(simObj)) {
                    survivors.add(simObj);
                } else {
                    eden.removeObject(obj);
                    eden.subtractUsed(simObj.getSize());
                    bytesCollected += simObj.getSize();
                }
            }
        }

        // Process from-survivor
        for (Object obj : new ArrayList<>(fromSurvivor.getObjects())) {
            if (obj instanceof SimulatedObject simObj) {
                if (reachable.contains(simObj)) {
                    simObj.incrementAge();
                    survivors.add(simObj);
                } else {
                    fromSurvivor.removeObject(obj);
                    fromSurvivor.subtractUsed(simObj.getSize());
                    bytesCollected += simObj.getSize();
                }
            }
        }

        // Move survivors to to-survivor or promote to old gen
        List<SimulatedObject> toPromote = new ArrayList<>();
        for (SimulatedObject obj : survivors) {
            if (obj.getAge() >= 15) { // Promotion threshold
                toPromote.add(obj);
                // Remove from young gen
                eden.removeObject(obj);
                fromSurvivor.removeObject(obj);
                eden.subtractUsed(obj.getSize());
                fromSurvivor.subtractUsed(obj.getSize());
            } else {
                // Move to to-survivor
                if (toSurvivor.canAllocate(obj.getSize())) {
                    toSurvivor.addObject(obj);
                    toSurvivor.addUsed(obj.getSize());
                    eden.removeObject(obj);
                    fromSurvivor.removeObject(obj);
                    eden.subtractUsed(obj.getSize());
                    fromSurvivor.subtractUsed(obj.getSize());
                }
            }
        }

        // Promote aged objects to old generation
        promoteObjects(toPromote);

        // Swap survivors
        youngGen.swapSurvivors();

        return bytesCollected;
    }

    /**
     * Major GC: Collect old generation.
     */
    private long majorGC() {
        log("Phase 2: Major GC (Old Generation)");

        OldGeneration oldGen = jvm.getHeap().getOldGen();

        // Mark reachable objects
        Set<SimulatedObject> reachable = mark();

        // Sweep and compact
        long bytesCollected = sweep(oldGen.getTenured(), reachable);

        // Compact (simulated)
        log("  Compacting old generation");

        return bytesCollected;
    }
}
