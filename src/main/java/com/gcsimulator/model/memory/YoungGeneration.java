package com.gcsimulator.model.memory;

import com.gcsimulator.model.objects.SimulatedObject;

/**
 * Represents the Young Generation of the heap, containing Eden and Survivor
 * spaces.
 */
public class YoungGeneration implements Memory {
    private final MemoryRegion eden;
    private final MemoryRegion survivor0;
    private final MemoryRegion survivor1;
    private int currentSurvivor; // 0 or 1

    public YoungGeneration(long edenSize, long survivorSize) {
        this.eden = new MemoryRegion("Eden", edenSize);
        this.survivor0 = new MemoryRegion("Survivor-0", survivorSize);
        this.survivor1 = new MemoryRegion("Survivor-1", survivorSize);
        this.currentSurvivor = 0;
    }

    public MemoryRegion getEden() {
        return eden;
    }

    public MemoryRegion getSurvivor0() {
        return survivor0;
    }

    public MemoryRegion getSurvivor1() {
        return survivor1;
    }

    public MemoryRegion getFromSurvivor() {
        return currentSurvivor == 0 ? survivor0 : survivor1;
    }

    public MemoryRegion getToSurvivor() {
        return currentSurvivor == 0 ? survivor1 : survivor0;
    }

    public void swapSurvivors() {
        currentSurvivor = 1 - currentSurvivor;
    }

    @Override
    public long getCapacity() {
        return eden.getCapacity() + survivor0.getCapacity() + survivor1.getCapacity();
    }

    @Override
    public long getUsed() {
        return eden.getUsed() + survivor0.getUsed() + survivor1.getUsed();
    }

    @Override
    public void clear() {
        eden.clear();
        survivor0.clear();
        survivor1.clear();
        currentSurvivor = 0;
    }

    public boolean canAllocate(long size) {
        return eden.canAllocate(size);
    }

    public void allocate(SimulatedObject obj) {
        eden.addObject(obj);
        eden.addUsed(obj.getSize());
    }
}
