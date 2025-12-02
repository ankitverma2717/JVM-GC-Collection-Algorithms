package com.gcsimulator.model.memory;

import com.gcsimulator.model.objects.SimulatedObject;

/**
 * Represents the Old Generation (Tenured) space of the heap.
 */
public class OldGeneration implements Memory {
    private final MemoryRegion tenured;

    public OldGeneration(long capacity) {
        this.tenured = new MemoryRegion("Old/Tenured", capacity);
    }

    public MemoryRegion getTenured() {
        return tenured;
    }

    @Override
    public long getCapacity() {
        return tenured.getCapacity();
    }

    @Override
    public long getUsed() {
        return tenured.getUsed();
    }

    @Override
    public void clear() {
        tenured.clear();
    }

    public boolean canAllocate(long size) {
        return tenured.canAllocate(size);
    }

    public void allocate(SimulatedObject obj) {
        tenured.addObject(obj);
        tenured.addUsed(obj.getSize());
    }

    public void promote(SimulatedObject obj) {
        allocate(obj);
    }
}
