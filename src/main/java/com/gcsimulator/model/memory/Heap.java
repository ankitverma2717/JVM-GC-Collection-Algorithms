package com.gcsimulator.model.memory;

import com.gcsimulator.model.objects.SimulatedObject;

/**
 * Represents the JVM Heap, containing Young and Old generations.
 */
public class Heap implements Memory {
    private final YoungGeneration youngGen;
    private final OldGeneration oldGen;

    public Heap(long youngSize, long oldSize) {
        // Young Gen: 1 Eden + 2 Survivors (Eden = 80%, each Survivor = 10%)
        long edenSize = (long) (youngSize * 0.8);
        long survivorSize = (long) (youngSize * 0.1);

        this.youngGen = new YoungGeneration(edenSize, survivorSize);
        this.oldGen = new OldGeneration(oldSize);
    }

    public YoungGeneration getYoungGen() {
        return youngGen;
    }

    public OldGeneration getOldGen() {
        return oldGen;
    }

    @Override
    public long getCapacity() {
        return youngGen.getCapacity() + oldGen.getCapacity();
    }

    @Override
    public long getUsed() {
        return youngGen.getUsed() + oldGen.getUsed();
    }

    @Override
    public void clear() {
        youngGen.clear();
        oldGen.clear();
    }

    public boolean canAllocate(long size) {
        return youngGen.canAllocate(size) || oldGen.canAllocate(size);
    }

    public void allocate(SimulatedObject obj) {
        if (youngGen.canAllocate(obj.getSize())) {
            youngGen.allocate(obj);
        } else if (oldGen.canAllocate(obj.getSize())) {
            // Direct allocation to old gen for large objects
            oldGen.allocate(obj);
        } else {
            throw new OutOfMemoryError("Heap space exhausted");
        }
    }
}
