package com.gcsimulator.gc.java17;

import com.gcsimulator.gc.*;
import com.gcsimulator.gc.java8.G1GC;
import com.gcsimulator.model.jvm.JVMSimulator;

/**
 * G1 GC Java 17: Enhanced version with improved pause time predictions
 * and better ergonomics.
 */
public class G1GCJava17 extends G1GC {

    @Override
    public GCAlgorithm getAlgorithm() {
        return GCAlgorithm.G1_JAVA17;
    }

    @Override
    public boolean collect() {
        // Java 17 improvements:
        // - Better concurrent refinement
        // - Improved NUMA awareness
        // - Enhanced string deduplication
        log("Using Java 17 G1 enhancements (improved concurrent refinement)");

        return super.collect();
    }
}
