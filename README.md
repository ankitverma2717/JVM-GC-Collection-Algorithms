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
  - Reset JVM state via menu

- **Performance Metrics**:
  - Total allocations and GC collections
  - Last and average pause times
  - Bytes collected per GC

- **Menu Functionality**:
  - **File Menu**: Reset JVM, Exit
  - **View Menu**: Clear Log
  - **Help Menu**: About, Algorithm Documentation

## Prerequisites

- **Java 17** or later (Java 21 recommended for full feature support)
- **No installation required!** - Project includes Gradle Wrapper

## Quick Start

### Clone the Repository

```bash
git clone https://github.com/ankitverma2717/JVM-GC-Collection-Algorithms.git
cd JVM-GC-Collection-Algorithms
```

### Run the Application

**Windows:**
```bash
.\gradlew.bat run
```

**Linux/Mac:**
```bash
./gradlew run
```

That's it! The Gradle Wrapper will automatically:
- Download Gradle 8.5 (first time only)
- Download all dependencies (JavaFX 21.0.1, etc.)
- Compile the project
- Launch the JavaFX application

## Building the Project

### Compile Only

```bash
# Windows
.\gradlew.bat compileJava

# Linux/Mac
./gradlew compileJava
```

### Build JAR

```bash
# Windows
.\gradlew.bat build

# Linux/Mac
./gradlew build
```

The executable JAR will be created in `build/libs/gc-simulator-1.0.0.jar`

### Clean Build

```bash
# Windows
.\gradlew.bat clean build

# Linux/Mac
./gradlew clean build
```

## Gradle Commands Reference

| Command | Description |
|---------|-------------|
| `.\gradlew.bat run` | Run the application |
| `.\gradlew.bat build` | Build executable JAR |
| `.\gradlew.bat compileJava` | Compile source code only |
| `.\gradlew.bat clean` | Delete build directory |
| `.\gradlew.bat test` | Run tests |
| `.\gradlew.bat tasks` | View all available tasks |
| `.\gradlew.bat dependencies` | View dependency tree |

## Usage Guide

### Getting Started

1. **Launch the application** using `.\gradlew.bat run`
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

#### Menu Bar

- **File → Reset JVM**: Resets the entire simulation
- **File → Exit**: Close the application
- **View → Clear Log**: Clear the event log
- **Help → About**: Application information
- **Help → Algorithm Documentation**: Documentation location

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
├── build.gradle                # Gradle build configuration
├── settings.gradle             # Gradle settings
├── gradlew.bat                 # Gradle wrapper (Windows)
├── gradlew                     # Gradle wrapper (Unix)
├── README.md                   # This file
├── algorithm.md                # Detailed GC algorithm documentation (539 lines)
├── gradle/
│   └── wrapper/               # Gradle wrapper files
└── src/
    └── main/
        ├── java/
        │   └── com/gcsimulator/
        │       ├── GCSimulatorApp.java         # Main entry point
        │       ├── controller/                 # UI controllers
        │       │   └── MainController.java    # Main UI logic
        │       ├── gc/                         # GC implementations
        │       │   ├── java8/                  # Java 8 algorithms
        │       │   │   ├── SerialGC.java
        │       │   │   ├── ParallelGC.java
        │       │   │   ├── ConcurrentMarkSweep.java
        │       │   │   └── G1GC.java
        │       │   ├── java17/                 # Java 17 algorithms
        │       │   │   ├── G1GCJava17.java
        │       │   │   ├── ZGC.java
        │       │   │   └── ShenandoahGC.java
        │       │   └── java21/                 # Java 21 algorithms
        │       │       ├── G1GCJava21.java
        │       │       ├── GenerationalZGC.java
        │       │       └── GenerationalShenandoah.java
        │       └── model/                      # JVM simulation model
        │           ├── jvm/                    # JVM components
        │           ├── memory/                 # Memory regions
        │           └── objects/                # Object representations
        └── resources/
            ├── fxml/
            │   └── main.fxml                   # JavaFX UI layout
            └── styles/
                └── application.css             # Modern dark theme
```

## Technology Stack

- **Language**: Java 17+
- **UI Framework**: JavaFX 21.0.1
- **Build Tool**: Gradle 8.5 (via Wrapper)
- **Architecture**: MVC Pattern
- **Styling**: CSS (Modern Dark Theme)

## Documentation

For detailed information about each GC algorithm, see [algorithm.md](algorithm.md), which includes:

- **Introduction to Garbage Collection**
- **GC Fundamentals** (memory structure, phases, roots)
- **Detailed algorithm descriptions** for all 10 collectors
- **Performance comparisons**
- **Configuration guides**
- **Use case recommendations**

## Development

### Project Requirements

- Java 17+ JDK
- Gradle is handled by wrapper (no installation needed)

### IDE Setup

**IntelliJ IDEA:**
1. Open project folder
2. IntelliJ will auto-detect Gradle
3. Run `Main` configuration or use `.\gradlew.bat run`

**Eclipse:**
1. Import as Gradle project
2. Use Gradle wrapper for build

**VS Code:**
1. Install Java Extension Pack
2. Open project folder
3. Terminal: `.\gradlew.bat run`

## Contributing

This is an educational project. If you find issues or have suggestions:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## Troubleshooting

### Gradle Wrapper fails to download

- Check internet connection
- If behind proxy, configure Gradle proxy settings
- Try again - downloads are cached after first successful run

### JavaFX not loading

- Ensure Java 17+ is installed: `java --version`
- JavaFX dependencies are automatically managed by Gradle
- Try clean build: `.\gradlew.bat clean build`

### Application won't start

- Check you're in the project directory
- Verify Java version is 17 or higher
- Try: `.\gradlew.bat clean run`

### OutOfMemoryError during simulation

- This is expected behavior when testing GC under memory pressure
- The simulator will automatically trigger GC
- Use **File → Reset JVM** or click Reset button

### Build errors

```bash
# Clean and rebuild
.\gradlew.bat clean build --refresh-dependencies
```

## Performance Tips

- Run with higher memory: `.\gradlew.bat run -Xmx1g`
- For faster compilation: `.\gradlew.bat build --parallel`
- Skip tests: `.\gradlew.bat build -x test`

## License

This project is for educational purposes.

## References

- [Oracle Java GC Tuning Guide](https://docs.oracle.com/en/java/javase/21/gctuning/)
- [JDK Enhancement Proposals (JEPs)](https://openjdk.org/jeps/)
- JEP 333: ZGC (Experimental)
- JEP 377: ZGC (Production)
- JEP 439: Generational ZGC
- [Gradle User Guide](https://docs.gradle.org/current/userguide/userguide.html)
- [JavaFX Documentation](https://openjfx.io/)

## Acknowledgments

Built with JavaFX and Gradle, simulating the incredible engineering work of the OpenJDK GC teams.

---

**GitHub**: [JVM-GC-Collection-Algorithms](https://github.com/ankitverma2717/JVM-GC-Collection-Algorithms)

**Author**: Ankit Verma

**Version**: 1.0.0
