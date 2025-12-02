package com.gcsimulator.gc.java17;

import com.gcsimulator.gc.*;
import com.gcsimulator.model.jvm.JVMSimulator;
import com.gcsimulator.model.memory.*;
import com.gcsimulator.model.objects.SimulatedObject;

import java.util.*;

/**
 * ZGC (Java 17): Scalable low-latency garbage collector.
 * Uses colored pointers and load barriers for concurrent operations.
 * Non-generational version (generational comes in Java 21).
 */
public class ZGC extends AbstractGarbageCollector {
    private static final int MAX_PAUSE_MS = 10; // Target max pause time

    private enum PointerColor {
        MARKED_0, MARKED_1, REMAPPED
    }

    private PointerColor currentColor;
    private final Map<SimulatedObject, PointerColor> coloredPointers;

    public ZGC() {
        this.currentColor = PointerColor.MARKED_0;
        this.coloredPointers = new HashMap<>();
    }

    @Override
    public GCAlgorithm getAlgorithm() {
        return GCAlgorithm.ZGC_JAVA17;
    }

    @Override
    public boolean collect() {
        long startTime = System.currentTimeMillis();
        jvm.incrementGCs();

        log("Starting ZGC collection (target max pause: " + MAX_PAUSE_MS + "ms)");
        jvm.fireEvent(new JVMSimulator.JVMEvent(
                JVMSimulator.JVMEvent.EventType.GC_STARTED,
                "ZGC started",
                null));

        long totalPauseTime = 0;
        long bytesCollected = 0;

        // Phase 1: Pause Mark Start (very short STW)
        long pause1Start = System.currentTimeMillis();
        log("Phase 1: Pause Mark Start (Stop-The-World)");
        pauseMarkStart();
        long pause1 = System.currentTimeMillis() - pause1Start;
        totalPauseTime += pause1;
        log("  Completed in " + pause1 + "ms");

        // Phase 2: Concurrent Mark
        log("Phase 2: Concurrent Mark (application running)");
        concurrentMark();
        log("  Concurrent mark completed");

        // Phase 3: Pause Mark End (very short STW)
        long pause2Start = System.currentTimeMillis();
        log("Phase 3: Pause Mark End (Stop-The-World)");
        pauseMarkEnd();
        long pause2 = System.currentTimeMillis() - pause2Start;
        totalPauseTime += pause2;
        log("  Completed in " + pause2 + "ms");

        // Phase 4: Concurrent Process/Relocate
        log("Phase 4: Concurrent Relocate (application running)");
        bytesCollected = concurrentRelocate();
        log("  Concurrent relocate completed");

        long totalTime = System.currentTimeMillis() - startTime;
        statistics.recordCollection(totalPauseTime, bytesCollected);

        log(String.format("ZGC completed: total=%dms, pause=%dms (%.1f%% pause), collected=%d bytes",
                totalTime, totalPauseTime, (totalPauseTime * 100.0 / totalTime), bytesCollected));
        jvm.fireEvent(new JVMSimulator.JVMEvent(
                JVMSimulator.JVMEvent.EventType.GC_COMPLETED,
                "ZGC completed",
                totalPauseTime));

        return true;
    }

    private void pauseMarkStart() {
        // Initialize marking with root set
        for (SimulatedObject root : jvm.getRootSet().getRoots()) {
            coloredPointers.put(root, currentColor);
        }
    }

    private void concurrentMark() {
        // Mark all reachable objects while application runs
        Queue<SimulatedObject> queue = new LinkedList<>();
        Set<SimulatedObject> visited = new HashSet<>();

        for (SimulatedObject root : jvm.getRootSet().getRoots()) {
            queue.add(root);
            visited.add(root);
        }

        while (!queue.isEmpty()) {
            SimulatedObject obj = queue.poll();
            coloredPointers.put(obj, currentColor);

            for (SimulatedObject ref : obj.getReferences()) {
                if (!visited.contains(ref)) {
                    visited.add(ref);
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

    private void pauseMarkEnd() {
        // Finalize marking
        // Flip color for next cycle
        currentColor = (currentColor == PointerColor.MARKED_0) ? PointerColor.MARKED_1 : PointerColor.MARKED_0;
    }

    private long concurrentRelocate() {
        // Relocate (evacuate) objects from pages with most garbage
        long bytesCollected = 0;

        OldGeneration oldGen = jvm.getHeap().getOldGen();
        YoungGeneration youngGen = jvm.getHeap().getYoungGen();

        // Collect from old gen
        List<Object> toRemove = new ArrayList<>();
        for (Object obj : oldGen.getTenured().getObjects()) {
            if (obj instanceof SimulatedObject simObj) {
                if (!coloredPointers.containsKey(simObj)) {
                    toRemove.add(obj);
                    bytesCollected += simObj.getSize();
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
            oldGen.getTenured().removeObject(obj);
            if (obj instanceof SimulatedObject simObj) {
                oldGen.getTenured().subtractUsed(simObj.getSize());
                coloredPointers.remove(simObj);
            }
        }

        log("  Relocated objects concurrently (using load barriers)");

        return bytesCollected;
    }
}
