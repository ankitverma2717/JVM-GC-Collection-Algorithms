# Java GC Simulator

A comprehensive JavaFX application that simulates and visualizes Garbage Collection algorithms across Java versions 8, 17, and 21.

## Overview

This simulator provides an interactive educational tool for understanding how different GC algorithms work in the Java Virtual Machine. It includes visual representations of memory regions, real-time statistics, and detailed logging of GC events.

## Features

- **10 Garbage Collection Algorithms**:
  - **Java 8**: Serial GC, Parallel GC, CMS, G1 GC
  - **Java 17**: Enhanced G1 GC, ZGC, Shenandoah GC
  - **Java 21**: Latest G1 GC, Generational ZGC, Generational Shenandoah

- **Interactive Visualization**:
  - Real-time memory usage bars for Eden, Survivors, Old Gen, Metaspace, and RAM
  - Live charts showing memory trends over time
  - Comprehensive event logging

- **Simulation Controls**:
  - Manual and automatic object allocation
  - Manual GC triggering
  - Play/Pause simulation mode
  - Reset JVM state

- **Performance Metrics**:
  - Total allocations and GC collections
  - Last and average pause times
  - Bytes collected per GC

## Prerequisites

- **Java 21** or later
- **Maven 3.x**
- **JavaFX 21.0.1** (automatically managed by Maven)

## Installation

### 1. Install Java 21

Make sure you have Java 21 installed and configured:

```bash
java -version
```

### 2. Install Maven

**Windows (using Chocolatey)**:
```powershell
choco install maven
```

**Windows (manual)**:
1. Download from https://maven.apache.org/download.cgi
2. Extract to `C:\Program Files\Apache\maven`
3. Add `bin` directory to PATH
4. Verify: `mvn --version`

## Building the Project

```bash
# Navigate to project directory
cd gc-simulator

# Clean and compile
mvn clean compile

# Run tests (if available)
mvn test

# Package as JAR
mvn package
```

## Running the Application

### Using Maven (Recommended)

```bash
mvn javafx:run
```

### Using Java Command

```bash
java --module-path path/to/javafx-sdk/lib \
     --add-modules javafx.controls,javafx.fxml \
     -jar target/gc-simulator-1.0.0.jar
```

## Usage Guide

### Getting Started

1. **Launch the application** using one of the methods above
2. **Select a GC algorithm** from the dropdown menu
3. **Click "Allocate Objects"** to manually allocate objects
4. **Click "Play"** to start automatic allocation simulation
5. **Observe the visualization** and statistics

### Understanding the UI

#### Memory Regions

- **Eden**: Where new objects are allocated
- **Survivor-0/1**: Temporary space for objects that survive minor GC
- **Old Generation**: Long-lived objects promoted from young generation
- **Metaspace**: Class metadata and constant pool
- **RAM**: Total physical memory used by JVM

#### Controls

- **GC Algorithm**: Select which algorithm to simulate
- **Allocate Objects**: Manually allocate 50 random objects
- **Trigger GC**: Force garbage collection immediately
- **Play/Pause**: Toggle automatic object allocation
- **Reset**: Clear all memory and restart the JVM

#### Statistics

- **Total Allocations**: Number of objects created
- **Total GC Collections**: Number of GC runs
- **Last Pause Time**: Duration of last GC in milliseconds
- **Avg Pause Time**: Average pause time across all GCs

### Testing Different Algorithms

#### Serial GC
- **Best for**: Small applications (<100MB heap)
- **Characteristics**: Simple, single-threaded, long pause times
- **Test**: Allocate many objects, observe long pauses

#### Parallel GC
- **Best for**: Throughput-oriented batch processing
- **Characteristics**: Multi-threaded, shorter pauses than Serial
- **Test**: Compare pause times with Serial GC

#### G1 GC
- **Best for**: Large heaps (>4GB), balanced performance
- **Characteristics**: Region-based, predictable pause times
- **Test**: Watch mixed collections combining young and old regions

#### ZGC
- **Best for**: Ultra-low latency applications
- **Characteristics**: Sub-10ms pauses, concurrent collection
- **Test**: Allocate heavily, observe minimal pauses

#### Generational ZGC (Java 21)
- **Best for**: Best of both worlds - low latency + high throughput
- **Characteristics**: <1ms young GC pauses
- **Test**: Compare with non-generational ZGC

## Project Structure

```
gc-simulator/
├── pom.xml                     # Maven configuration
├── README.md                   # This file
├── algorithm.md                # Detailed GC algorithm documentation
└── src/
    └── main/
        ├── java/
        │   └── com/gcsimulator/
        │       ├── GCSimulatorApp.java         # Main entry point
        │       ├── controller/                 # UI controllers
        │       ├── gc/                         # GC implementations
        │       │   ├── java8/                  # Java 8 algorithms
        │       │   ├── java17/                 # Java 17 algorithms
        │       │   └── java21/                 # Java 21 algorithms
        │       └── model/                      # JVM simulation model
        │           ├── jvm/                    # JVM components
        │           ├── memory/                 # Memory regions
        │           └── objects/                # Object representations
        └── resources/
            ├── fxml/                           # JavaFX layouts
            └── styles/                         # CSS styling
```

## Documentation

For detailed information about each GC algorithm, see [algorithm.md](algorithm.md), which includes:

- **Introduction to Garbage Collection**
- **GC Fundamentals** (memory structure, phases, roots)
- **Detailed algorithm descriptions** for all 10 collectors
- **Performance comparisons**
- **Configuration guides**
- **Use case recommendations**

## Contributing

This is an educational project. If you find issues or have suggestions:

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Submit a pull request

## License

This project is for educational purposes.

## References

- [Oracle Java GC Tuning Guide](https://docs.oracle.com/en/java/javase/21/gctuning/)
- [JDK Enhancement Proposals (JEPs)](https://openjdk.org/jeps/)
- JEP 333: ZGC (Experimental)
- JEP 377: ZGC (Production)
- JEP 439: Generational ZGC

## Troubleshooting

### Maven not found

- Ensure Maven is installed and added to PATH
- Restart terminal after installation
- Verify with `mvn --version`

### JavaFX errors

- The project uses JavaFX 21.0.1 which is managed by Maven
- Ensure you're using Java 21
- Dependencies will download automatically

### OutOfMemoryError during simulation

- This is expected behavior when testing GC under memory pressure
- The simulator will automatically trigger GC
- Click "Reset" to clear memory and start fresh

## Acknowledgments

Built with JavaFX and Maven, simulating the incredible engineering work of the OpenJDK GC teams.
