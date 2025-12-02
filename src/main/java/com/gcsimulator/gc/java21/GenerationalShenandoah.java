package com.gcsimulator.gc.java21;

import com.gcsimulator.gc.*;
import com.gcsimulator.gc.java17.ShenandoahGC;
import com.gcsimulator.model.jvm.JVMSimulator;

/**
 * Generational Shenandoah (Java 21): Shenandoah with generational mode.
 * Separates young and old objects for better throughput.
 */
public class GenerationalShenandoah extends ShenandoahGC {

    @Override
    public GCAlgorithm getAlgorithm() {
        return GCAlgorithm.GENERATIONAL_SHENANDOAH;
    }

    @Override
    public boolean collect() {
        log("Using Generational Shenandoah (generational mode enabled)");
        log("  Separate young generation evacuation for improved throughput");

        // Use parent's collection logic with generational tracking
        return super.collect();
    }
}
