package com.gcsimulator.model.memory;

import java.util.*;

/**
 * Simulates the physical RAM available to the JVM.
 */
public class SimulatedRAM implements Memory {
    private final long totalCapacity;
    private long used;
    private final Map<String, Long> allocations;

    public SimulatedRAM(long totalCapacity) {
        this.totalCapacity = totalCapacity;
        this.used = 0;
        this.allocations = new HashMap<>();
    }

    /**
     * Allocate a block of memory with a given name.
     */
    public boolean allocate(String name, long size) {
        if (used + size > totalCapacity) {
            return false;
        }
        allocations.put(name, allocations.getOrDefault(name, 0L) + size);
        used += size;
        return true;
    }

    /**
     * Deallocate a block of memory.
     */
    public void deallocate(String name, long size) {
        Long current = allocations.get(name);
        if (current != null) {
            long newSize = Math.max(0, current - size);
            if (newSize == 0) {
                allocations.remove(name);
            } else {
                allocations.put(name, newSize);
            }
            used = Math.max(0, used - size);
        }
    }

    public Map<String, Long> getAllocations() {
        return Collections.unmodifiableMap(allocations);
    }

    @Override
    public long getCapacity() {
        return totalCapacity;
    }

    @Override
    public long getUsed() {
        return used;
    }

    @Override
    public void clear() {
        allocations.clear();
        used = 0;
    }

    /**
     * Get memory fragmentation as a percentage.
     */
    public double getFragmentation() {
        // Simplified fragmentation calculation
        int blocks = allocations.size();
        if (blocks <= 1) {
            return 0.0;
        }
        // More blocks = more fragmentation
        return Math.min(1.0, blocks / 100.0);
    }
}
