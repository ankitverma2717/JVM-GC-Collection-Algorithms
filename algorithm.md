# Garbage Collection Algorithms - Comprehensive Guide

## Table of Contents
1. [Introduction to Garbage Collection](#introduction)
2. [GC Fundamentals](#fundamentals)
3. [Java 8 Algorithms](#java-8-algorithms)
4. [Java 17 Algorithms](#java-17-algorithms)
5. [Java 21 Algorithms](#java-21-algorithms)
6. [Performance Comparison](#performance-comparison)
7. [Configuration Guide](#configuration-guide)

---

## Introduction to Garbage Collection

Garbage Collection (GC) is an automatic memory management mechanism in Java that identifies and reclaims memory occupied by objects that are no longer reachable from the application. This allows developers to focus on application logic rather than manual memory management.

### Why Garbage Collection?

- **Memory Safety**: Prevents memory leaks and dangling pointers
- **Developer Productivity**: Eliminates manual memory management
- **Application Stability**: Reduces memory-related crashes

---

## GC Fundamentals

### Memory Structure

The JVM heap is divided into several regions:

```
+----------------------------------------------------------+
|                       JVM HEAP                            |
+----------------------------------------------------------+
|  Young Generation           |   Old Generation (Tenured) |
|  +----------------------+   |                            |
|  | Eden Space           |   |                            |
|  +----------------------+   |                            |
|  | Survivor Space 0 (S0)|   |                            |
|  +----------------------+   |                            |
|  | Survivor Space 1 (S1)|   |                            |
|  +----------------------+   |                            |
+----------------------------------------------------------+
|                    Metaspace (Java 8+)                    |
|                or PermGen (Java 7-)                       |
+----------------------------------------------------------+
```

### Generational Hypothesis

The generational hypothesis states:
1. **Most objects die young**: Short-lived objects can be collected quickly
2. **Old objects rarely reference young objects**: Cross-generational references are uncommon

Based on this, the heap is divided into:
- **Young Generation**: Newly allocated objects (Eden + 2 Survivor spaces)
- **Old Generation**: Long-lived objects promoted from young gen
- **Metaspace/PermGen**: Class metadata and constants

### GC Phases

1. **Mark**: Identify all reachable objects from GC roots
2. **Sweep**: Reclaim memory from unreachable objects
3. **Compact**: Move objects together to reduce fragmentation (optional)

### GC Roots

Objects considered as starting points for reachability:
- Local variables in stack frames
- Static variables
- JNI references
- Synchronization monitors

---

## Java 8 Algorithms

### 1. Serial GC (`-XX:+UseSerialGC`)

**Type**: Single-threaded, stop-the-world  
**Best For**: Small applications, single-core systems

#### How It Works

**Minor GC (Young Generation)**:
1. Stop all application threads (Stop-The-World pause)
2. Mark all reachable objects from roots
3. Copy live objects from Eden → Survivor space
4. Copy live objects from one Survivor → other Survivor
5. Promote objects with age ≥ 15 to Old Gen
6. Clear Eden and from-Survivor
7. Resume application threads

**Major GC (Old Generation)**:
1. Stop all application threads
2. Mark all reachable objects (mark phase)
3. Remove unreachable objects (sweep phase)
4. Compact remaining objects (compact phase)
5. Resume application threads

#### Characteristics
- **Pause Time**: High (10-100ms+)
- **Throughput**: Moderate
- **Memory Overhead**: Low
- **Fragmentation**: None (compacts)

#### Use Cases
- Applications with heaps < 100MB
- Single-processor machines
- When pause times are acceptable

---

### 2. Parallel GC (`-XX:+UseParallelGC`)

**Type**: Multi-threaded, stop-the-world  
**Best For**: Multi-core systems, throughput-oriented applications

#### How It Works

Similar to Serial GC but uses multiple threads for both minor and major collections.

**Key Differences**:
- Multiple GC threads work in parallel
- Scales with available CPU cores
- Optimized for maximum throughput

#### Characteristics
- **Pause Time**: Moderate (shorter than Serial on multi-core)
- **Throughput**: High (maximizes application run time)
- **Memory Overhead**: Moderate
- **Fragmentation**: None (compacts)

#### Configuration
```
-XX:+UseParallelGC
-XX:ParallelGCThreads=<N>     # Number of GC threads
-XX:MaxGCPauseMillis=<N>      # Target max pause time
-XX:GCTimeRatio=<N>           # Throughput goal (default 99)
```

---

### 3. CMS (Concurrent Mark Sweep) (`-XX:+UseConcMarkSweepGC`)

**Type**: Concurrent, low-latency  
**Best For**: Applications requiring low pause times

#### How It Works

**Phase 1: Initial Mark** (STW - short)
- Mark objects directly reachable from roots
- Very brief pause

**Phase 2: Concurrent Mark** (concurrent)
- Traverse object graph while application runs
- Mark all reachable objects

**Phase 3: Concurrent Preclean** (concurrent)
- Identify objects modified during concurrent mark

**Phase 4: Remark** (STW - short)
- Catch any objects modified during concurrent phases
- Brief pause

**Phase 5: Concurrent Sweep** (concurrent)
- Reclaim memory from unmarked objects
- Application continues running

#### Characteristics
- **Pause Time**: Very low (typically < 50ms)
- **Throughput**: Lower than Parallel GC
- **Memory Overhead**: High (needs extra CPU)
- **Fragmentation**: Yes (doesn't compact)

#### Drawbacks
- **Fragmentation**: No compaction leads to fragmented memory
- **Floating Garbage**: Objects that become garbage during concurrent phases aren't collected
- **CPU Usage**: Uses CPU cycles during concurrent phases
- **Deprecated**: Removed in Java 14

---

### 4. G1 GC (Garbage First) (`-XX:+UseG1GC`)

**Type**: Region-based, partially concurrent  
**Best For**: Large heaps (> 4GB), predictable pause times

#### How It Works

**Heap Organization**:
- Heap divided into equal-sized regions (typically 1-32MB each)
- Each region can be: Eden, Survivor, Old, or Humongous (for large objects)

**Young Generation Collection**:
1. Stop-The-World pause
2. Evacuate (copy) live objects from Eden regions
3. Evacuate survivors with age check
4. Promote aged objects to old regions

**Mixed Collection**:
1. Concurrent marking identifies regions with most garbage
2. Collect young regions + selected old regions
3. Prioritizes regions with most reclaimable space ("Garbage First")

**Phases**:
1. **Young GC**: Collect young regions
2. **Concurrent Mark**: Mark live objects concurrently
   - Initial Mark (STW)
   - Root Region Scan
   - Concurrent Mark
   - Remark (STW)
   - Cleanup (STW)
3. **Mixed GC**: Collect young + old regions

#### Characteristics
- **Pause Time**: Predictable, configurable (default 200ms)
- **Throughput**: Good
- **Memory Overhead**: Moderate
- **Fragmentation**: Minimal (evacuates regions)

#### Configuration
```
-XX:+UseG1GC
-XX:MaxGCPauseMillis=200      # Target pause time
-XX:G1HeapRegionSize=<N>      # Region size (1-32MB)
-XX:InitiatingHeapOccupancyPercent=45  # Trigger for concurrent cycle
```

---

## Java 17 Algorithms

### 5. Enhanced G1 GC

**Type**: Improved region-based collector  
**New in Java 17**: Enhanced concurrent refinement, better pause predictions

#### Improvements Over Java 8
- Better concurrent refinement thread management
- Improved NUMA (Non-Uniform Memory Access) awareness
- Enhanced string deduplication
- More accurate pause time predictions

---

### 6. ZGC (Z Garbage Collector) (`-XX:+UseZGC`)

**Type**: Scalable, ultra-low latency  
**Best For**: Large heaps (multi-TB), latency-sensitive applications

#### How It Works

**Key Innovation: Colored Pointers**
- Uses 64-bit pointers with metadata in unused bits
- Bits indicate object state: Marked0, Marked1, Remapped

**Phases**:

**Phase 1: Pause Mark Start** (STW - < 1ms)
- Initialize marking with root set

**Phase 2: Concurrent Mark** (concurrent)
- Traverse object graph
- Mark all reachable objects
- Application continues with load barriers

**Phase 3: Pause Mark End** (STW - < 1ms)
- Finalize marking

**Phase 4: Concurrent Prepare for Relocate** (concurrent)
- Select pages to evacuate

**Phase 5: Pause Relocate Start** (STW - < 1ms)
- Relocate root set

**Phase 6: Concurrent Relocate** (concurrent)
- Move objects to new locations
- Update references using load barriers

#### Characteristics
- **Pause Time**: < 10ms (typically < 1ms)
- **Throughput**: Good (90%+)
- **Memory Overhead**: Moderate
- **Scalability**: Handles multi-TB heaps

#### Load Barriers
- Every object access goes through a load barrier
- Checks pointer color and remaps if needed
- Enables concurrent relocation

#### Configuration
```
-XX:+UseZGC
-XX:SoftMaxHeapSize=<N>       # Soft max heap
-XX:ConcGCThreads=<N>         # Concurrent threads
```

---

### 7. Shenandoah GC (`-XX:+UseShenandoahGC`)

**Type**: Concurrent, low-latency  
**Best For**: Large heaps, consistent low latency

#### How It Works

**Key Innovation: Brooks Forwarding Pointers**
- Each object has a forwarding pointer
- Enables concurrent evacuation

**Phases**:

**Phase 1: Init Mark** (STW - brief)
- Mark root set

**Phase 2: Concurrent Mark** (concurrent)
- Traverse object graph
- Mark all reachable objects

**Phase 3: Final Mark** (STW - brief)
- Re-scan modified objects

**Phase 4: Concurrent Cleanup** (concurrent)
- Reclaim regions with no live objects

**Phase 5: Concurrent Evacuation** (concurrent)
- Copy live objects to new regions
- Update forwarding pointers
- Application continues running

**Phase 6: Init Update Refs** (STW - brief)

**Phase 7: Concurrent Update References** (concurrent)
- Update all references to relocated objects

**Phase 8: Final Update Refs** (STW - brief)

#### Characteristics
- **Pause Time**: Very low (~10ms)
- **Throughput**: Slightly lower than ZGC
- **Memory Overhead**: Higher (forwarding pointers)
- **Scalability**: Good for large heaps

#### Configuration
```
-XX:+UseShenandoahGC
-XX:ShenandoahGCHeuristics=<adaptive|static|compact>
```

---

## Java 21 Algorithms

### 8. Latest G1 GC

**Improvements in Java 21**:
- Optimized remembered set (card table) processing
- Better NUMA support
- Reduced memory footprint
- Improved concurrent refinement

---

### 9. Generational ZGC (`-XX:+UseZGC -XX:+ZGenerational`)

**Type**: ZGC with generational mode  
**New in Java 21**: Production-ready generational support

#### How It Works

Extends ZGC with separate young and old generations:

**Young Generation Collection** (frequent):
- Very fast (sub-ms pauses)
- Collects recently allocated objects
- Most objects die here

**Old Generation Collection** (infrequent):
- Only when needed
- Collects long-lived objects

#### Benefits Over Non-Generational ZGC
- **Better Throughput**: 10-20% improvement
- **Lower Memory Overhead**: Reduced live set scanning
- **Even Lower Pauses**: Young collections are faster

#### Characteristics
- **Pause Time**: < 1ms (young), < 10ms (old)
- **Throughput**: Higher than non-generational ZGC
- **Memory Overhead**: Lower than non-generational

#### Configuration
```
-XX:+UseZGC
-XX:+ZGenerational
```

---

### 10. Generational Shenandoah

**Type**: Shenandoah with generational mode  
**Status**: Experimental in Java 21

#### Improvements
- Separate young generation evacuation
- Improved throughput over non-generational mode
- Lower pause times for young collections

---

## Performance Comparison

### Pause Time vs Throughput

```
                    Pause Time
                    (Lower is better)
                    ↓
    ZGC (Gen)      |█
    ZGC            |██
    Shenandoah     |███
    G1 GC          |██████
    Parallel GC    |████████████
    Serial GC      |███████████████
                    
                    Throughput
                    (Higher is better)
                    →
    Parallel GC    ████████████████
    G1 GC          █████████████
    ZGC (Gen)      ████████████
    ZGC            ███████████
    Shenandoah     ██████████
    Serial GC      █████
```

### Use Case Matrix

| Algorithm | Heap Size | Pause Time | Throughput | CPU Usage |
|-----------|-----------|------------|------------|-----------|
| Serial GC | < 100MB | 10-100ms | Moderate | Low |
| Parallel GC | 100MB-4GB | 50-500ms | High | Moderate |
| CMS | 1-4GB | < 50ms | Moderate | High |
| G1 GC | > 4GB | ~200ms | Good | Moderate |
| ZGC | > 1GB | < 10ms | Good | Moderate |
| Shenandoah | > 1GB | ~10ms | Moderate | High |
| ZGC (Gen) | > 1GB | < 1ms | High | Moderate |

---

## Configuration Guide

### Choosing the Right GC

**For Small Applications (< 100MB heap)**:
```bash
-XX:+UseSerialGC
```

**For Batch Processing (throughput priority)**:
```bash
-XX:+UseParallelGC -XX:MaxGCPauseMillis=1000
```

**For Web Applications (balanced)**:
```bash
-XX:+UseG1GC -XX:MaxGCPauseMillis=200
```

**For Latency-Sensitive Applications**:
```bash
# Java 21+
-XX:+UseZGC -XX:+ZGenerational

# Java 17
-XX:+UseZGC
# or
-XX:+UseShenandoahGC
```

### Common Tuning Parameters

```bash
# Heap Size
-Xms<size>         # Initial heap size
-Xmx<size>         # Maximum heap size

# Young Generation
-Xmn<size>         # Young generation size

# GC Logging (Java 9+)
-Xlog:gc*:file=gc.log:time,level,tags

# Thread Count
-XX:ParallelGCThreads=<N>     # Parallel GC threads
-XX:ConcGCThreads=<N>         # Concurrent GC threads
```

---

## Summary

### Evolution of Java GC

**Java 8**: 
- Focus on throughput (Parallel GC)
- Introduction of low-latency options (CMS, G1)

**Java 17**:
- Modern low-latency collectors (ZGC, Shenandoah) become production-ready
- Removal of CMS

**Java 21**:
- Generational modes for ultra-low latency
- ZGC and Shenandoah improvements
- G1 optimizations

### Key Takeaways

1. **No perfect GC**: Each algorithm has trade-offs
2. **Application-specific**: Choose based on requirements
3. **Measure, don't guess**: Use GC logs and monitoring
4. **Start simple**: Begin with G1, tune if needed
5. **Modern collectors**: ZGC/Shenandoah for latency-critical apps

---

## References

- [JDK Enhancement Proposals (JEPs)](https://openjdk.org/jeps/)
- [Oracle Java GC Tuning Guide](https://docs.oracle.com/en/java/javase/21/gctuning/)
- [Understanding Java Garbage Collection](https://www.oracle.com/webfolder/technetwork/tutorials/obe/java/gc01/index.html)
- JEP 333: ZGC (Experimental)
- JEP 377: ZGC (Production)
- JEP 439: Generational ZGC
