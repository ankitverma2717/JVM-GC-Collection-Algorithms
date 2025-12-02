package com.gcsimulator.gc.java17;

import com.gcsimulator.gc.*;
import com.gcsimulator.model.jvm.JVMSimulator;
import com.gcsimulator.model.memory.*;
import com.gcsimulator.model.objects.SimulatedObject;

import java.util.*;

/**
 * Shenandoah GC (Java 17): Low-latency collector with concurrent evacuation.
 * Uses Brooks forwarding pointers for concurrent compaction.
 */
public class ShenandoahGC extends AbstractGarbageCollector {
    private final Map<SimulatedObject, SimulatedObject> forwardingPointers;

    public ShenandoahGC() {
        this.forwardingPointers = new HashMap<>();
    }

    @Override
    public GCAlgorithm getAlgorithm() {
        return GCAlgorithm.SHENANDOAH;
    }

    @Override
    public boolean collect() {
        long startTime = System.currentTimeMillis();
        jvm.incrementGCs();

        log("Starting Shenandoah collection");
        jvm.fireEvent(new JVMSimulator.JVMEvent(
                JVMSimulator.JVMEvent.EventType.GC_STARTED,
                "Shenandoah GC started",
                null));

        long totalPauseTime = 0;
        long bytesCollected = 0;

        // Phase 1: Init Mark (short STW)
        long pause1Start = System.currentTimeMillis();
        log("Phase 1: Init Mark (Stop-The-World)");
        Set<SimulatedObject> roots = initMark();
        long pause1 = System.currentTimeMillis() - pause1Start;
        totalPauseTime += pause1;
        log("  Init mark completed in " + pause1 + "ms");

        // Phase 2: Concurrent Mark
        log("Phase 2: Concurrent Mark (application running)");
        Set<SimulatedObject> reachable = concurrentMark(roots);
        log("  Concurrent mark completed");

        // Phase 3: Final Mark (short STW)
        long pause2Start = System.currentTimeMillis();
        log("Phase 3: Final Mark (Stop-The-World)");
        finalMark(reachable);
        long pause2 = System.currentTimeMillis() - pause2Start;
        totalPauseTime += pause2;
        log("  Final mark completed in " + pause2 + "ms");

        // Phase 4: Concurrent Evacuation
        log("Phase 4: Concurrent Evacuation (application running)");
        concurrentEvacuation(reachable);
        log("  Concurrent evacuation completed");

        // Phase 5: Concurrent Cleanup
        log("Phase 5: Concurrent Cleanup (application running)");
        bytesCollected = concurrentCleanup(reachable);
        log("  Concurrent cleanup completed");

        long totalTime = System.currentTimeMillis() - startTime;
        statistics.recordCollection(totalPauseTime, bytesCollected);

        log(String.format("Shenandoah completed: total=%dms, pause=%dms, collected=%d bytes",
                totalTime, totalPauseTime, bytesCollected));
        jvm.fireEvent(new JVMSimulator.JVMEvent(
                JVMSimulator.JVMEvent.EventType.GC_COMPLETED,
                "Shenandoah GC completed",
                totalPauseTime));

        return true;
    }

    private Set<SimulatedObject> initMark() {
        Set<SimulatedObject> roots = new HashSet<>(jvm.getRootSet().getRoots());
        for (SimulatedObject root : roots) {
            root.setMarked(true);
        }
        return roots;
    }

    private Set<SimulatedObject> concurrentMark(Set<SimulatedObject> roots) {
        Set<SimulatedObject> reachable = new HashSet<>();
        Queue<SimulatedObject> queue = new LinkedList<>(roots);
        reachable.addAll(roots);

        while (!queue.isEmpty()) {
            SimulatedObject obj = queue.poll();
            for (SimulatedObject ref : obj.getReferences()) {
                if (!reachable.contains(ref)) {
                    reachable.add(ref);
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

        return reachable;
    }

    private void finalMark(Set<SimulatedObject> reachable) {
        // Re-scan modified objects
        for (SimulatedObject root : jvm.getRootSet().getRoots()) {
            if (!reachable.contains(root)) {
                reachable.add(root);
            }
        }
    }

    private void concurrentEvacuation(Set<SimulatedObject> reachable) {
        // Evacuate (move) objects concurrently using Brooks pointers
        log("  Using Brooks forwarding pointers for concurrent evacuation");

        forwardingPointers.clear();

        // In a real implementation, objects would be copied to new locations
        // and forwarding pointers would redirect references
        for (SimulatedObject obj : reachable) {
            // Simulate forwarding pointer (in reality, objects are copied)
            forwardingPointers.put(obj, obj);

            // Simulate concurrent execution
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private long concurrentCleanup(Set<SimulatedObject> reachable) {
        long bytesCollected = 0;

        // Clean up unreachable objects
        OldGeneration oldGen = jvm.getHeap().getOldGen();
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
