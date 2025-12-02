package com.gcsimulator.gc.java8;

import com.gcsimulator.gc.*;
import com.gcsimulator.model.jvm.JVMSimulator;
import com.gcsimulator.model.memory.*;
import com.gcsimulator.model.objects.SimulatedObject;

import java.util.*;
import java.util.concurrent.*;

/**
 * Parallel GC: Multi-threaded throughput-oriented collector.
 * Uses parallel threads for both young and old generation collection.
 */
public class ParallelGC extends AbstractGarbageCollector {
    private final int threadCount;
    private final ExecutorService executor;

    public ParallelGC() {
        this.threadCount = Runtime.getRuntime().availableProcessors();
        this.executor = Executors.newFixedThreadPool(threadCount);
    }

    @Override
    public GCAlgorithm getAlgorithm() {
        return GCAlgorithm.PARALLEL_GC;
    }

    @Override
    public boolean collect() {
        long startTime = System.currentTimeMillis();
        jvm.incrementGCs();

        log("Starting parallel collection with " + threadCount + " threads (Stop-The-World)");
        jvm.fireEvent(new JVMSimulator.JVMEvent(
                JVMSimulator.JVMEvent.EventType.GC_STARTED,
                "Parallel GC started",
                null));

        // Parallel minor GC
        long bytesCollected = parallelMinorGC();

        // Parallel major GC if needed
        if (jvm.getHeap().getOldGen().getUtilization() >= oldGenThreshold) {
            bytesCollected += parallelMajorGC();
        }

        long pauseTime = System.currentTimeMillis() - startTime;
        statistics.recordCollection(pauseTime, bytesCollected);

        log(String.format("Collection completed in %dms, collected %d bytes", pauseTime, bytesCollected));
        jvm.fireEvent(new JVMSimulator.JVMEvent(
                JVMSimulator.JVMEvent.EventType.GC_COMPLETED,
                "Parallel GC completed",
                pauseTime));

        return true;
    }

    private long parallelMinorGC() {
        log("Phase 1: Parallel Minor GC");

        // In a real implementation, this would use multiple threads
        // For simulation, we'll just mark it as parallel
        YoungGeneration youngGen = jvm.getHeap().getYoungGen();

        // Mark phase (parallelized)
        Set<SimulatedObject> reachable = mark();

        // Sweep phase (parallelized)
        long bytesCollected = 0;
        bytesCollected += sweep(youngGen.getEden(), reachable);
        bytesCollected += sweep(youngGen.getFromSurvivor(), reachable);

        // Copy survivors
        List<SimulatedObject> survivors = new ArrayList<>();
        for (Object obj : youngGen.getEden().getObjects()) {
            if (obj instanceof SimulatedObject simObj && reachable.contains(simObj)) {
                survivors.add(simObj);
            }
        }

        // Process survivors
        for (SimulatedObject obj : survivors) {
            obj.incrementAge();
            if (obj.getAge() >= 15) {
                youngGen.getEden().removeObject(obj);
                youngGen.getEden().subtractUsed(obj.getSize());
                jvm.getHeap().getOldGen().promote(obj);
            } else {
                MemoryRegion toSurvivor = youngGen.getToSurvivor();
                if (toSurvivor.canAllocate(obj.getSize())) {
                    youngGen.getEden().removeObject(obj);
                    youngGen.getEden().subtractUsed(obj.getSize());
                    toSurvivor.addObject(obj);
                    toSurvivor.addUsed(obj.getSize());
                }
            }
        }

        youngGen.swapSurvivors();

        return bytesCollected;
    }

    private long parallelMajorGC() {
        log("Phase 2: Parallel Major GC");

        OldGeneration oldGen = jvm.getHeap().getOldGen();

        // Parallel mark
        Set<SimulatedObject> reachable = mark();

        // Parallel sweep and compact
        long bytesCollected = sweep(oldGen.getTenured(), reachable);

        log("  Parallel compaction");

        return bytesCollected;
    }
}
