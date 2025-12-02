package com.gcsimulator.gc.java21;

import com.gcsimulator.gc.*;
import com.gcsimulator.model.jvm.JVMSimulator;
import com.gcsimulator.model.memory.*;
import com.gcsimulator.model.objects.SimulatedObject;

import java.util.*;

/**
 * Generational ZGC (Java 21): ZGC with generational support.
 * Separates young and old generations for better performance.
 */
public class GenerationalZGC extends AbstractGarbageCollector {
    private final Map<SimulatedObject, Integer> objectGenerations;
    private int currentGeneration;

    public GenerationalZGC() {
        this.objectGenerations = new HashMap<>();
        this.currentGeneration = 0;
    }

    @Override
    public GCAlgorithm getAlgorithm() {
        return GCAlgorithm.GENERATIONAL_ZGC;
    }

    @Override
    public boolean collect() {
        long startTime = System.currentTimeMillis();
        jvm.incrementGCs();

        log("Starting Generational ZGC collection");
        jvm.fireEvent(new JVMSimulator.JVMEvent(
                JVMSimulator.JVMEvent.EventType.GC_STARTED,
                "Generational ZGC started",
                null));

        long totalPauseTime = 0;
        long bytesCollected = 0;

        // Young Generation Collection (frequent, very fast)
        log("Phase 1: Young Generation Collection");
        long youngStart = System.currentTimeMillis();
        bytesCollected += collectYoungGeneration();
        long youngPause = System.currentTimeMillis() - youngStart;
        totalPauseTime += youngPause;
        log("  Young GC completed in " + youngPause + "ms");

        // Old Generation Collection (infrequent)
        if (shouldCollectOldGeneration()) {
            log("Phase 2: Old Generation Collection");
            long oldStart = System.currentTimeMillis();
            bytesCollected += collectOldGeneration();
            long oldPause = System.currentTimeMillis() - oldStart;
            totalPauseTime += oldPause;
            log("  Old GC completed in " + oldPause + "ms");
        }

        currentGeneration++;

        long totalTime = System.currentTimeMillis() - startTime;
        statistics.recordCollection(totalPauseTime, bytesCollected);

        log(String.format("Generational ZGC completed: total=%dms, pause=%dms, collected=%d bytes",
                totalTime, totalPauseTime, bytesCollected));
        jvm.fireEvent(new JVMSimulator.JVMEvent(
                JVMSimulator.JVMEvent.EventType.GC_COMPLETED,
                "Generational ZGC completed",
                totalPauseTime));

        return true;
    }

    private long collectYoungGeneration() {
        // Very short pause - mark and relocate young objects
        YoungGeneration youngGen = jvm.getHeap().getYoungGen();

        Set<SimulatedObject> reachable = mark();
        long bytesCollected = 0;

        // Collect Eden
        List<Object> toRemove = new ArrayList<>();
        for (Object obj : youngGen.getEden().getObjects()) {
            if (obj instanceof SimulatedObject simObj) {
                if (!reachable.contains(simObj)) {
                    toRemove.add(obj);
                    bytesCollected += simObj.getSize();
                } else {
                    // Track generation
                    objectGenerations.put(simObj, currentGeneration);
                }
            }
        }

        for (Object obj : toRemove) {
            youngGen.getEden().removeObject(obj);
            if (obj instanceof SimulatedObject simObj) {
                youngGen.getEden().subtractUsed(simObj.getSize());
                objectGenerations.remove(simObj);
            }
        }

        log("  Using colored pointers and load barriers (generational mode)");

        return bytesCollected;
    }

    private boolean shouldCollectOldGeneration() {
        return jvm.getHeap().getOldGen().getUtilization() >= 0.7;
    }

    private long collectOldGeneration() {
        // Concurrent old generation collection
        OldGeneration oldGen = jvm.getHeap().getOldGen();

        log("  Concurrent marking and relocation of old objects");

        Set<SimulatedObject> reachable = mark();
        long bytesCollected = 0;

        List<Object> toRemove = new ArrayList<>();
        for (Object obj : oldGen.getTenured().getObjects()) {
            if (obj instanceof SimulatedObject simObj && !reachable.contains(simObj)) {
                toRemove.add(obj);
                bytesCollected += simObj.getSize();
            }
        }

        for (Object obj : toRemove) {
            oldGen.getTenured().removeObject(obj);
            if (obj instanceof SimulatedObject simObj) {
                oldGen.getTenured().subtractUsed(simObj.getSize());
                objectGenerations.remove(simObj);
            }

            // Simulate concurrent execution
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        return bytesCollected;
    }
}
