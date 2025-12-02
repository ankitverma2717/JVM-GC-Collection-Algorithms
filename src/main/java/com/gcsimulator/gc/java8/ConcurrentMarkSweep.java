package com.gcsimulator.gc.java8;

import com.gcsimulator.gc.*;
import com.gcsimulator.model.jvm.JVMSimulator;
import com.gcsimulator.model.memory.*;
import com.gcsimulator.model.objects.SimulatedObject;

import java.util.*;

/**
 * CMS (Concurrent Mark Sweep): Low-latency collector with concurrent marking.
 * Minimizes pause times but may cause fragmentation.
 */
public class ConcurrentMarkSweep extends AbstractGarbageCollector {
    private Set<SimulatedObject> markedObjects;

    public ConcurrentMarkSweep() {
        this.markedObjects = new HashSet<>();
    }

    @Override
    public GCAlgorithm getAlgorithm() {
        return GCAlgorithm.CMS;
    }

    @Override
    public boolean collect() {
        long startTime = System.currentTimeMillis();
        jvm.incrementGCs();

        log("Starting CMS collection");
        jvm.fireEvent(new JVMSimulator.JVMEvent(
                JVMSimulator.JVMEvent.EventType.GC_STARTED,
                "CMS GC started",
                null));

        long bytesCollected = 0;
        long totalPauseTime = 0;

        // Phase 1: Initial Mark (STW - short pause)
        long pause1Start = System.currentTimeMillis();
        log("Phase 1: Initial Mark (Stop-The-World)");
        initialMark();
        long pause1 = System.currentTimeMillis() - pause1Start;
        totalPauseTime += pause1;
        log("  Initial mark completed in " + pause1 + "ms");

        // Phase 2: Concurrent Mark (application continues)
        log("Phase 2: Concurrent Mark (application running)");
        concurrentMark();
        log("  Concurrent mark completed");

        // Phase 3: Remark (STW - short pause)
        long pause2Start = System.currentTimeMillis();
        log("Phase 3: Remark (Stop-The-World)");
        remark();
        long pause2 = System.currentTimeMillis() - pause2Start;
        totalPauseTime += pause2;
        log("  Remark completed in " + pause2 + "ms");

        // Phase 4: Concurrent Sweep (application continues)
        log("Phase 4: Concurrent Sweep (application running)");
        bytesCollected = concurrentSweep();
        log("  Concurrent sweep completed");

        long totalTime = System.currentTimeMillis() - startTime;
        statistics.recordCollection(totalPauseTime, bytesCollected);

        log(String.format("CMS collection completed: total=%dms, pause=%dms, collected=%d bytes",
                totalTime, totalPauseTime, bytesCollected));
        jvm.fireEvent(new JVMSimulator.JVMEvent(
                JVMSimulator.JVMEvent.EventType.GC_COMPLETED,
                "CMS GC completed",
                totalPauseTime));

        return true;
    }

    /**
     * Initial Mark: Mark objects directly reachable from roots (STW).
     */
    private void initialMark() {
        markedObjects.clear();
        for (SimulatedObject root : jvm.getRootSet().getRoots()) {
            markedObjects.add(root);
            root.setMarked(true);
        }
    }

    /**
     * Concurrent Mark: Trace object graph while application runs.
     */
    private void concurrentMark() {
        Queue<SimulatedObject> queue = new LinkedList<>(markedObjects);

        while (!queue.isEmpty()) {
            SimulatedObject obj = queue.poll();
            for (SimulatedObject ref : obj.getReferences()) {
                if (!markedObjects.contains(ref)) {
                    markedObjects.add(ref);
                    ref.setMarked(true);
                    queue.add(ref);
                }
            }

            // Simulate concurrent execution
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Remark: Catch any modifications made during concurrent mark (STW).
     */
    private void remark() {
        // Re-scan objects that may have changed during concurrent mark
        Set<SimulatedObject> newMarks = new HashSet<>();
        for (SimulatedObject root : jvm.getRootSet().getRoots()) {
            if (!markedObjects.contains(root)) {
                newMarks.add(root);
            }
        }
        markedObjects.addAll(newMarks);
    }

    /**
     * Concurrent Sweep: Reclaim unmarked objects while application runs.
     */
    private long concurrentSweep() {
        OldGeneration oldGen = jvm.getHeap().getOldGen();
        MemoryRegion tenured = oldGen.getTenured();

        List<Object> toRemove = new ArrayList<>();
        long bytesCollected = 0;

        for (Object obj : tenured.getObjects()) {
            if (obj instanceof SimulatedObject simObj) {
                if (!markedObjects.contains(simObj)) {
                    toRemove.add(obj);
                    bytesCollected += simObj.getSize();
                } else {
                    simObj.setMarked(false); // Reset for next collection
                }
            }

            // Simulate concurrent execution
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        for (Object obj : toRemove) {
            tenured.removeObject(obj);
            if (obj instanceof SimulatedObject simObj) {
                tenured.subtractUsed(simObj.getSize());
            }
        }

        // Note: CMS doesn't compact, which can lead to fragmentation
        log("  WARNING: No compaction - fragmentation may occur");

        return bytesCollected;
    }
}
