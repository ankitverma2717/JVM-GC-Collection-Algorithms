package com.gcsimulator.model.memory;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a memory region (e.g., Eden, Survivor, Old Gen).
 */
public class MemoryRegion implements Memory {
    private final String name;
    private final long capacity;
    private long used;
    private final List<Object> objects;

    public MemoryRegion(String name, long capacity) {
        this.name = name;
        this.capacity = capacity;
        this.used = 0;
        this.objects = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    @Override
    public long getCapacity() {
        return capacity;
    }

    @Override
    public long getUsed() {
        return used;
    }

    public void addUsed(long bytes) {
        this.used += bytes;
    }

    public void subtractUsed(long bytes) {
        this.used = Math.max(0, this.used - bytes);
    }

    public List<Object> getObjects() {
        return objects;
    }

    public void addObject(Object obj) {
        objects.add(obj);
    }

    public void removeObject(Object obj) {
        objects.remove(obj);
    }

    @Override
    public void clear() {
        objects.clear();
        used = 0;
    }

    public boolean canAllocate(long size) {
        return getAvailable() >= size;
    }
}
