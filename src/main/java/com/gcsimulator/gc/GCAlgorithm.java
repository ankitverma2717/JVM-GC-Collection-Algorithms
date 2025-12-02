package com.gcsimulator.gc;

/**
 * Enum representing different GC algorithms across Java versions.
 */
public enum GCAlgorithm {
    // Java 8
    SERIAL_GC("Serial GC", "Java 8", "Single-threaded, stop-the-world collector"),
    PARALLEL_GC("Parallel GC", "Java 8", "Multi-threaded throughput collector"),
    CMS("Concurrent Mark Sweep", "Java 8", "Low-latency concurrent collector"),
    G1_JAVA8("G1 GC", "Java 8", "Region-based, predictable pause times"),

    // Java 17
    G1_JAVA17("G1 GC (Enhanced)", "Java 17", "Improved G1 with better pause times"),
    ZGC_JAVA17("ZGC", "Java 17", "Scalable low-latency collector"),
    SHENANDOAH("Shenandoah GC", "Java 17", "Concurrent evacuation collector"),

    // Java 21
    G1_JAVA21("G1 GC (Latest)", "Java 21", "Latest G1 improvements"),
    GENERATIONAL_ZGC("Generational ZGC", "Java 21", "ZGC with generational mode"),
    GENERATIONAL_SHENANDOAH("Generational Shenandoah", "Java 21", "Shenandoah with generational mode");

    private final String displayName;
    private final String javaVersion;
    private final String description;

    GCAlgorithm(String displayName, String javaVersion, String description) {
        this.displayName = displayName;
        this.javaVersion = javaVersion;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getJavaVersion() {
        return javaVersion;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return displayName + " (" + javaVersion + ")";
    }
}
