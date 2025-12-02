package com.gcsimulator.gc;

import com.gcsimulator.model.jvm.JVMSimulator;
import com.gcsimulator.model.objects.SimulatedObject;
import com.gcsimulator.model.memory.*;

import java.util.*;

/**
 * Abstract base class for garbage collectors with common functionality.
 */
public abstract class AbstractGarbageCollector implements GarbageCollector {
    protected JVMSimulator jvm;
    protected GCStatistics statistics;
    protected double youngGenThreshold = 0.85; // Collect when 85% full
    protected double oldGenThreshold = 0.75; // Collect when 75% full

    public AbstractGarbageCollector() {
        this.statistics = new GCStatistics();
    }

    @Override
    public void initialize(JVMSimulator jvm) {
        this.jvm = jvm;
    }

    @Override
    public GCStatistics getStatistics() {
        return statistics;
    }

    @Override
    public void reset() {
        statistics.reset();
    }

    @Override
    public boolean shouldCollect() {
        Heap heap = jvm.getHeap();

        // Check if young gen is above threshold
        if (heap.getYoungGen().getUtilization() >= youngGenThreshold) {
            return true;
        }

        // Check if old gen is above threshold
        if (heap.getOldGen().getUtilization() >= oldGenThreshold) {
            return true;
        }

        return false;
    }

    /**
     * Mark phase: mark all reachable objects starting from roots.
     */
    protected Set<SimulatedObject> mark() {
        Set<SimulatedObject> reachable = new HashSet<>();
        Queue<SimulatedObject> queue = new LinkedList<>();

        // Start with root set
        for (SimulatedObject root : jvm.getRootSet().getRoots()) {
            queue.add(root);
            reachable.add(root);
            root.setMarked(true);
        }

        // BFS to mark all reachable objects
        while (!queue.isEmpty()) {
            SimulatedObject obj = queue.poll();
            for (SimulatedObject ref : obj.getReferences()) {
                if (!reachable.contains(ref)) {
                    reachable.add(ref);
                    ref.setMarked(true);
                    queue.add(ref);
                }
            }
        }

        return reachable;
    }

    /**
     * Sweep phase: remove unmarked objects.
     */
    protected long sweep(MemoryRegion region, Set<SimulatedObject> reachable) {
        List<Object> toRemove = new ArrayList<>();
        long bytesCollected = 0;

        for (Object obj : region.getObjects()) {
            if (obj instanceof SimulatedObject simObj) {
                if (!reachable.contains(simObj)) {
                    toRemove.add(obj);
                    bytesCollected += simObj.getSize();
                } else {
                    simObj.setMarked(false); // Reset mark for next collection
                }
            }
        }

        for (Object obj : toRemove) {
            region.removeObject(obj);
            if (obj instanceof SimulatedObject simObj) {
                region.subtractUsed(simObj.getSize());
                jvm.fireEvent(new JVMSimulator.JVMEvent(
                        JVMSimulator.JVMEvent.EventType.OBJECT_COLLECTED,
                        "Collected object: " + simObj,
                        simObj));
            }
        }

        return bytesCollected;
    }

    /**
     * Promote objects from young to old generation.
     */
    protected void promoteObjects(List<SimulatedObject> objects) {
        OldGeneration oldGen = jvm.getHeap().getOldGen();

        for (SimulatedObject obj : objects) {
            if (oldGen.canAllocate(obj.getSize())) {
                oldGen.promote(obj);
                jvm.fireEvent(new JVMSimulator.JVMEvent(
                        JVMSimulator.JVMEvent.EventType.PROMOTION,
                        "Promoted object: " + obj,
                        obj));
            }
        }
    }

    /**
     * Log a GC event.
     */
    protected void log(String message) {
        System.out.println("[" + getName() + "] " + message);
    }
}
