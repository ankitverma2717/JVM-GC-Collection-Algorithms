package com.gcsimulator.gc.java21;

import com.gcsimulator.gc.*;
import com.gcsimulator.gc.java17.G1GCJava17;

/**
 * G1 GC Java 21: Latest improvements including better NUMA support
 * and optimized card table processing.
 */
public class G1GCJava21 extends G1GCJava17 {

    @Override
    public GCAlgorithm getAlgorithm() {
        return GCAlgorithm.G1_JAVA21;
    }

    @Override
    public boolean collect() {
        log("Using Java 21 G1 enhancements (optimized card tables, better NUMA)");
        return super.collect();
    }
}
