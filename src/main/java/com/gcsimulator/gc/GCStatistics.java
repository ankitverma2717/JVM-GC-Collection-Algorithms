package com.gcsimulator.gc;

/**
 * Statistics collected during garbage collection.
 */
public class GCStatistics {
    private long totalCollections;
    private long totalPauseTimeMs;
    private long totalBytesCollected;
    private long minPauseTimeMs;
    private long maxPauseTimeMs;
    private long lastPauseTimeMs;
    private long lastCollectionTime;

    public GCStatistics() {
        this.totalCollections = 0;
        this.totalPauseTimeMs = 0;
        this.totalBytesCollected = 0;
        this.minPauseTimeMs = Long.MAX_VALUE;
        this.maxPauseTimeMs = 0;
        this.lastPauseTimeMs = 0;
        this.lastCollectionTime = 0;
    }

    public void recordCollection(long pauseTimeMs, long bytesCollected) {
        totalCollections++;
        totalPauseTimeMs += pauseTimeMs;
        totalBytesCollected += bytesCollected;
        lastPauseTimeMs = pauseTimeMs;
        lastCollectionTime = System.currentTimeMillis();

        if (pauseTimeMs < minPauseTimeMs) {
            minPauseTimeMs = pauseTimeMs;
        }
        if (pauseTimeMs > maxPauseTimeMs) {
            maxPauseTimeMs = pauseTimeMs;
        }
    }

    public long getTotalCollections() {
        return totalCollections;
    }

    public long getTotalPauseTimeMs() {
        return totalPauseTimeMs;
    }

    public double getAveragePauseTimeMs() {
        return totalCollections > 0 ? (double) totalPauseTimeMs / totalCollections : 0.0;
    }

    public long getTotalBytesCollected() {
        return totalBytesCollected;
    }

    public long getMinPauseTimeMs() {
        return minPauseTimeMs == Long.MAX_VALUE ? 0 : minPauseTimeMs;
    }

    public long getMaxPauseTimeMs() {
        return maxPauseTimeMs;
    }

    public long getLastPauseTimeMs() {
        return lastPauseTimeMs;
    }

    public long getLastCollectionTime() {
        return lastCollectionTime;
    }

    public void reset() {
        totalCollections = 0;
        totalPauseTimeMs = 0;
        totalBytesCollected = 0;
        minPauseTimeMs = Long.MAX_VALUE;
        maxPauseTimeMs = 0;
        lastPauseTimeMs = 0;
        lastCollectionTime = 0;
    }

    @Override
    public String toString() {
        return String.format(
                "GC Stats: Collections=%d, Total Pause=%dms, Avg Pause=%.2fms, Min=%dms, Max=%dms, Collected=%d bytes",
                totalCollections, totalPauseTimeMs, getAveragePauseTimeMs(),
                getMinPauseTimeMs(), maxPauseTimeMs, totalBytesCollected);
    }
}
