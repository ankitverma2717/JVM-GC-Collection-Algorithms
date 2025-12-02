package com.gcsimulator.model.jvm;

import com.gcsimulator.model.memory.*;
import com.gcsimulator.model.objects.RootSet;
import com.gcsimulator.model.objects.SimulatedObject;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Main JVM simulator orchestrating memory, stack, and GC.
 */
public class JVMSimulator {
    private final SimulatedRAM ram;
    private final Heap heap;
    private final Metaspace metaspace;
    private final List<com.gcsimulator.model.memory.Stack> threadStacks;
    private final RootSet rootSet;
    private final AllocationEngine allocationEngine;
    private final List<JVMEventListener> listeners;

    private long totalAllocations;
    private long totalGCs;

    public JVMSimulator(long ramSize, long heapSize, long metaspaceSize) {
        this.ram = new SimulatedRAM(ramSize);

        // Allocate heap from RAM (60% young, 40% old)
        long youngSize = (long) (heapSize * 0.6);
        long oldSize = (long) (heapSize * 0.4);
        this.heap = new Heap(youngSize, oldSize);

        // Metaspace (Java 8+) is dynamic
        this.metaspace = new Metaspace(metaspaceSize / 2, metaspaceSize, true);

        this.threadStacks = new ArrayList<>();
        this.rootSet = new RootSet();
        this.allocationEngine = new AllocationEngine(this);
        this.listeners = new CopyOnWriteArrayList<>();

        // Allocate memory in RAM
        ram.allocate("Heap", heapSize);
        ram.allocate("Metaspace", metaspaceSize / 2);

        this.totalAllocations = 0;
        this.totalGCs = 0;
    }

    public SimulatedRAM getRam() {
        return ram;
    }

    public Heap getHeap() {
        return heap;
    }

    public Metaspace getMetaspace() {
        return metaspace;
    }

    public List<com.gcsimulator.model.memory.Stack> getThreadStacks() {
        return Collections.unmodifiableList(threadStacks);
    }

    public RootSet getRootSet() {
        return rootSet;
    }

    public AllocationEngine getAllocationEngine() {
        return allocationEngine;
    }

    public void addStack(com.gcsimulator.model.memory.Stack stack) {
        threadStacks.add(stack);
        ram.allocate("Stack-" + stack.getThreadName(), stack.getCapacity());
    }

    public void removeStack(com.gcsimulator.model.memory.Stack stack) {
        threadStacks.remove(stack);
        ram.deallocate("Stack-" + stack.getThreadName(), stack.getCapacity());
    }

    public void incrementAllocations() {
        totalAllocations++;
    }

    public void incrementGCs() {
        totalGCs++;
    }

    public long getTotalAllocations() {
        return totalAllocations;
    }

    public long getTotalGCs() {
        return totalGCs;
    }

    public void addEventListener(JVMEventListener listener) {
        listeners.add(listener);
    }

    public void removeEventListener(JVMEventListener listener) {
        listeners.remove(listener);
    }

    public void fireEvent(JVMEvent event) {
        for (JVMEventListener listener : listeners) {
            listener.onEvent(event);
        }
    }

    /**
     * Reset the JVM state.
     */
    public void reset() {
        heap.clear();
        metaspace.clear();
        threadStacks.forEach(com.gcsimulator.model.memory.Stack::clear);
        rootSet.clear();
        totalAllocations = 0;
        totalGCs = 0;
    }

    /**
     * Event listener interface for JVM events.
     */
    public interface JVMEventListener {
        void onEvent(JVMEvent event);
    }

    /**
     * JVM event types.
     */
    public static class JVMEvent {
        private final EventType type;
        private final String message;
        private final Object data;

        public JVMEvent(EventType type, String message, Object data) {
            this.type = type;
            this.message = message;
            this.data = data;
        }

        public EventType getType() {
            return type;
        }

        public String getMessage() {
            return message;
        }

        public Object getData() {
            return data;
        }

        public enum EventType {
            OBJECT_ALLOCATED,
            OBJECT_COLLECTED,
            GC_STARTED,
            GC_COMPLETED,
            PROMOTION,
            OUT_OF_MEMORY
        }
    }
}
