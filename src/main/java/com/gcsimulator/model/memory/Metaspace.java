package com.gcsimulator.model.memory;

/**
 * Represents the Metaspace (Java 8+) or PermGen (Java 7-) for class metadata.
 */
public class Metaspace implements Memory {
    private final MemoryRegion region;
    private final boolean isDynamic; // true for Metaspace, false for PermGen
    private long maxCapacity;

    public Metaspace(long initialCapacity, long maxCapacity, boolean isDynamic) {
        this.region = new MemoryRegion(isDynamic ? "Metaspace" : "PermGen", initialCapacity);
        this.maxCapacity = maxCapacity;
        this.isDynamic = isDynamic;
    }

    public boolean isDynamic() {
        return isDynamic;
    }

    public long getMaxCapacity() {
        return maxCapacity;
    }

    /**
     * Simulates dynamic expansion of Metaspace.
     */
    public boolean expandIfNeeded(long requiredSize) {
        if (!isDynamic) {
            return false;
        }

        long needed = requiredSize - getAvailable();
        if (needed > 0 && getCapacity() < maxCapacity) {
            // Expand by the needed amount or 25%, whichever is larger
            long expansion = Math.max(needed, getCapacity() / 4);
            long newCapacity = Math.min(getCapacity() + expansion, maxCapacity);
            // Note: In a real implementation, you'd resize the region
            return true;
        }
        return false;
    }

    @Override
    public long getCapacity() {
        return region.getCapacity();
    }

    @Override
    public long getUsed() {
        return region.getUsed();
    }

    @Override
    public void clear() {
        region.clear();
    }

    public void loadClass(long size) {
        region.addUsed(size);
    }

    public void unloadClass(long size) {
        region.subtractUsed(size);
    }
}
