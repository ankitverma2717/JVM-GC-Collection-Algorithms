package com.gcsimulator.gc;

import com.gcsimulator.model.jvm.JVMSimulator;

/**
 * Base interface for all garbage collectors.
 */
public interface GarbageCollector {
    /**
     * Initialize the garbage collector with the JVM instance.
     */
    void initialize(JVMSimulator jvm);

    /**
     * Perform garbage collection.
     * 
     * @return true if collection was performed, false otherwise
     */
    boolean collect();

    /**
     * Determine if garbage collection should run.
     */
    boolean shouldCollect();

    /**
     * Get the algorithm type.
     */
    GCAlgorithm getAlgorithm();

    /**
     * Get statistics for this collector.
     */
    GCStatistics getStatistics();

    /**
     * Reset the collector state.
     */
    void reset();

    /**
     * Get the name of the collector.
     */
    default String getName() {
        return getAlgorithm().getDisplayName();
    }
}
