package com.gcsimulator.model.memory;

/**
 * Base interface for all memory components in the simulator.
 */
public interface Memory {
    /**
     * Get the total capacity of this memory region in bytes.
     */
    long getCapacity();
    
    /**
     * Get the currently used memory in bytes.
     */
    long getUsed();
    
    /**
     * Get the available memory in bytes.
     */
    default long getAvailable() {
        return getCapacity() - getUsed();
    }
    
    /**
     * Get the utilization as a percentage (0.0 to 1.0).
     */
    default double getUtilization() {
        return getCapacity() > 0 ? (double) getUsed() / getCapacity() : 0.0;
    }
    
    /**
     * Clear all contents from this memory region.
     */
    void clear();
}
