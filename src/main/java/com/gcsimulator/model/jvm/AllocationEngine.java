package com.gcsimulator.model.jvm;

import com.gcsimulator.model.objects.SimulatedObject;

import java.util.Random;

/**
 * Handles object allocation in the JVM.
 */
public class AllocationEngine {
    private final JVMSimulator jvm;
    private final Random random;

    public AllocationEngine(JVMSimulator jvm) {
        this.jvm = jvm;
        this.random = new Random();
    }

    /**
     * Allocate a new object in the heap.
     */
    public SimulatedObject allocate(long size, String type) {
        SimulatedObject obj = new SimulatedObject(size, type);

        try {
            jvm.getHeap().allocate(obj);
            jvm.incrementAllocations();
            jvm.fireEvent(new JVMSimulator.JVMEvent(
                    JVMSimulator.JVMEvent.EventType.OBJECT_ALLOCATED,
                    "Allocated object: " + obj,
                    obj));
            return obj;
        } catch (OutOfMemoryError e) {
            jvm.fireEvent(new JVMSimulator.JVMEvent(
                    JVMSimulator.JVMEvent.EventType.OUT_OF_MEMORY,
                    "Failed to allocate object of size " + size,
                    null));
            throw e;
        }
    }

    /**
     * Allocate a random object.
     */
    public SimulatedObject allocateRandom() {
        // Random size between 100 bytes and 10KB
        long size = 100 + random.nextInt(10000);
        String[] types = { "User", "Order", "Product", "Session", "Cache", "Buffer" };
        String type = types[random.nextInt(types.length)];
        return allocate(size, type);
    }

    /**
     * Create a random object graph (objects referencing each other).
     */
    public SimulatedObject allocateObjectGraph(int depth, int breadth) {
        SimulatedObject root = allocateRandom();

        if (depth > 0) {
            for (int i = 0; i < breadth; i++) {
                SimulatedObject child = allocateObjectGraph(depth - 1, breadth);
                root.addReference(child);
            }
        }

        return root;
    }
}
