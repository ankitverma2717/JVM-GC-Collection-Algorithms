package com.gcsimulator.gc.java8;

import com.gcsimulator.gc.*;
import com.gcsimulator.model.jvm.JVMSimulator;
import com.gcsimulator.model.memory.*;
import com.gcsimulator.model.objects.SimulatedObject;

import java.util.*;

/**
 * G1 GC (Garbage First): Region-based collector with predictable pause times.
 * Divides heap into regions and collects regions with most garbage first.
 */
public class G1GC extends AbstractGarbageCollector {
    private static final int REGION_COUNT = 16;
    private final List<HeapRegion> regions;
    private int youngRegionCount;

    public G1GC() {
        this.regions = new ArrayList<>();
        this.youngRegionCount = 0;
    }

    @Override
    public void initialize(JVMSimulator jvm) {
        super.initialize(jvm);

        // Divide heap into regions
        long heapSize = jvm.getHeap().getCapacity();
        long regionSize = heapSize / REGION_COUNT;

        for (int i = 0; i < REGION_COUNT; i++) {
            regions.add(new HeapRegion(i, regionSize, RegionType.FREE));
        }

        // Initially allocate some regions as young
        youngRegionCount = REGION_COUNT / 3;
        for (int i = 0; i < youngRegionCount; i++) {
            regions.get(i).setType(RegionType.EDEN);
        }
    }

    @Override
    public GCAlgorithm getAlgorithm() {
        return GCAlgorithm.G1_JAVA8;
    }

    @Override
    public boolean collect() {
        long startTime = System.currentTimeMillis();
        jvm.incrementGCs();

        log("Starting G1 collection");
        jvm.fireEvent(new JVMSimulator.JVMEvent(
                JVMSimulator.JVMEvent.EventType.GC_STARTED,
                "G1 GC started",
                null));

        long bytesCollected = 0;

        // Young Generation Collection (always)
        bytesCollected += youngGC();

        // Mixed Collection (if old gen regions need collection)
        if (shouldDoMixedGC()) {
            bytesCollected += mixedGC();
        }

        long pauseTime = System.currentTimeMillis() - startTime;
        statistics.recordCollection(pauseTime, bytesCollected);

        log(String.format("G1 collection completed in %dms, collected %d bytes", pauseTime, bytesCollected));
        jvm.fireEvent(new JVMSimulator.JVMEvent(
                JVMSimulator.JVMEvent.EventType.GC_COMPLETED,
                "G1 GC completed",
                pauseTime));

        return true;
    }

    private long youngGC() {
        log("Phase 1: Young Generation Collection");

        long bytesCollected = 0;
        Set<SimulatedObject> reachable = mark();

        // Collect all Eden regions
        for (HeapRegion region : regions) {
            if (region.getType() == RegionType.EDEN) {
                List<Object> toRemove = new ArrayList<>();
                for (Object obj : region.getObjects()) {
                    if (obj instanceof SimulatedObject simObj && !reachable.contains(simObj)) {
                        toRemove.add(obj);
                        bytesCollected += simObj.getSize();
                    }
                }

                for (Object obj : toRemove) {
                    region.removeObject(obj);
                }

                // Reset region after collection
                region.clear();
                region.setType(RegionType.EDEN);
            }
        }

        return bytesCollected;
    }

    private boolean shouldDoMixedGC() {
        // Check if any old regions are full
        for (HeapRegion region : regions) {
            if (region.getType() == RegionType.OLD && region.getUtilization() > 0.8) {
                return true;
            }
        }
        return false;
    }

    private long mixedGC() {
        log("Phase 2: Mixed Collection (Young + Old regions)");

        long bytesCollected = 0;
        Set<SimulatedObject> reachable = mark();

        // Select old regions with most garbage (Garbage First heuristic)
        List<HeapRegion> oldRegions = regions.stream()
                .filter(r -> r.getType() == RegionType.OLD)
                .sorted((r1, r2) -> Double.compare(r2.getGarbageRatio(), r1.getGarbageRatio()))
                .limit(3) // Collect top 3 regions with most garbage
                .toList();

        for (HeapRegion region : oldRegions) {
            List<Object> toRemove = new ArrayList<>();
            for (Object obj : region.getObjects()) {
                if (obj instanceof SimulatedObject simObj && !reachable.contains(simObj)) {
                    toRemove.add(obj);
                    bytesCollected += simObj.getSize();
                }
            }

            for (Object obj : toRemove) {
                region.removeObject(obj);
            }

            log("  Collected region " + region.getId() + " with " +
                    String.format("%.1f%% garbage", region.getGarbageRatio() * 100));
        }

        return bytesCollected;
    }

    /**
     * Represents a heap region in G1.
     */
    private static class HeapRegion extends MemoryRegion {
        private final int id;
        private RegionType type;

        public HeapRegion(int id, long capacity, RegionType type) {
            super("Region-" + id, capacity);
            this.id = id;
            this.type = type;
        }

        public int getId() {
            return id;
        }

        public RegionType getType() {
            return type;
        }

        public void setType(RegionType type) {
            this.type = type;
        }

        public double getGarbageRatio() {
            // Simplified: assume 50% of used space is garbage
            return getUtilization() * 0.5;
        }
    }

    private enum RegionType {
        FREE, EDEN, SURVIVOR, OLD, HUMONGOUS
    }
}
