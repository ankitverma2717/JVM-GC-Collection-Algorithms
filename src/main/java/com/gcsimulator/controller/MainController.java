package com.gcsimulator.controller;

import com.gcsimulator.gc.*;
import com.gcsimulator.gc.java8.*;
import com.gcsimulator.gc.java17.*;
import com.gcsimulator.gc.java21.*;
import com.gcsimulator.model.jvm.JVMSimulator;
import com.gcsimulator.model.objects.SimulatedObject;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.Duration;

import java.util.*;

/**
 * Main controller for the GC Simulator UI.
 */
public class MainController {
    @FXML
    private ComboBox<GCAlgorithm> gcAlgorithmComboBox;
    @FXML
    private Button allocateButton;
    @FXML
    private Button triggerGCButton;
    @FXML
    private Button resetButton;
    @FXML
    private Button playPauseButton;

    @FXML
    private Pane heapVisualizationPane;
    @FXML
    private Pane stackVisualizationPane;

    @FXML
    private ProgressBar edenUsageBar;
    @FXML
    private ProgressBar survivor0UsageBar;
    @FXML
    private ProgressBar survivor1UsageBar;
    @FXML
    private ProgressBar oldGenUsageBar;
    @FXML
    private ProgressBar metaspaceUsageBar;
    @FXML
    private ProgressBar ramUsageBar;

    @FXML
    private Label edenLabel;
    @FXML
    private Label survivor0Label;
    @FXML
    private Label survivor1Label;
    @FXML
    private Label oldGenLabel;
    @FXML
    private Label metaspaceLabel;
    @FXML
    private Label ramLabel;

    @FXML
    private Label totalAllocationsLabel;
    @FXML
    private Label totalGCsLabel;
    @FXML
    private Label lastPauseTimeLabel;
    @FXML
    private Label avgPauseTimeLabel;

    @FXML
    private TextArea logTextArea;
    @FXML
    private LineChart<Number, Number> memoryChart;

    // Model
    private JVMSimulator jvm;
    private GarbageCollector currentGC;
    private Timeline simulationTimeline;
    private boolean isPlaying;

    // Chart data
    private XYChart.Series<Number, Number> heapSeries;
    private XYChart.Series<Number, Number> youngGenSeries;
    private XYChart.Series<Number, Number> oldGenSeries;
    private int chartTime = 0;

    @FXML
    public void initialize() {
        // Initialize JVM with 512MB RAM, 256MB heap, 64MB metaspace
        jvm = new JVMSimulator(512 * 1024 * 1024L, 256 * 1024 * 1024L, 64 * 1024 * 1024L);

        // Set up event listener
        jvm.addEventListener(this::onJVMEvent);

        // Initialize GC algorithm combo box
        gcAlgorithmComboBox.setItems(FXCollections.observableArrayList(GCAlgorithm.values()));
        gcAlgorithmComboBox.setValue(GCAlgorithm.G1_JAVA8);
        gcAlgorithmComboBox.setOnAction(e -> switchGCAlgorithm());

        // Initialize default GC
        switchGCAlgorithm();

        // Set up buttons
        allocateButton.setOnAction(e -> allocateObjects());
        triggerGCButton.setOnAction(e -> triggerGC());
        resetButton.setOnAction(e -> reset());
        playPauseButton.setOnAction(e -> togglePlayPause());

        // Set up memory chart
        heapSeries = new XYChart.Series<>();
        heapSeries.setName("Total Heap");
        youngGenSeries = new XYChart.Series<>();
        youngGenSeries.setName("Young Gen");
        oldGenSeries = new XYChart.Series<>();
        oldGenSeries.setName("Old Gen");

        memoryChart.getData().addAll(heapSeries, youngGenSeries, oldGenSeries);
        memoryChart.setCreateSymbols(false);
        memoryChart.setAnimated(false);

        // Set up simulation timeline
        simulationTimeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> simulationStep()));
        simulationTimeline.setCycleCount(Timeline.INDEFINITE);

        isPlaying = false;

        // Initial update
        updateUI();

        log("GC Simulator initialized");
        log("Heap: 256MB (Young: 153MB, Old: 102MB)");
        log("RAM: 512MB, Metaspace: 64MB");
    }

    private void switchGCAlgorithm() {
        GCAlgorithm algorithm = gcAlgorithmComboBox.getValue();

        currentGC = switch (algorithm) {
            case SERIAL_GC -> new SerialGC();
            case PARALLEL_GC -> new ParallelGC();
            case CMS -> new ConcurrentMarkSweep();
            case G1_JAVA8 -> new G1GC();
            case G1_JAVA17 -> new G1GCJava17();
            case ZGC_JAVA17 -> new ZGC();
            case SHENANDOAH -> new ShenandoahGC();
            case G1_JAVA21 -> new G1GCJava21();
            case GENERATIONAL_ZGC -> new GenerationalZGC();
            case GENERATIONAL_SHENANDOAH -> new GenerationalShenandoah();
        };

        currentGC.initialize(jvm);
        log("Switched to: " + algorithm);
    }

    private void allocateObjects() {
        int count = 50;
        for (int i = 0; i < count; i++) {
            SimulatedObject obj = jvm.getAllocationEngine().allocateRandom();

            // Randomly add some objects to root set (10% chance)
            if (Math.random() < 0.1) {
                jvm.getRootSet().addRoot(obj);
            }
        }

        log("Allocated " + count + " objects");
        updateUI();

        // Auto-trigger GC if needed
        if (currentGC.shouldCollect()) {
            log("Memory threshold reached - triggering GC");
            triggerGC();
        }
    }

    private void triggerGC() {
        log("--- Manual GC Triggered ---");
        currentGC.collect();
        updateUI();
        log("--- GC Complete ---");
    }

    private void reset() {
        jvm.reset();
        currentGC.reset();
        chartTime = 0;
        heapSeries.getData().clear();
        youngGenSeries.getData().clear();
        oldGenSeries.getData().clear();
        log("JVM Reset");
        updateUI();
    }

    private void togglePlayPause() {
        isPlaying = !isPlaying;
        if (isPlaying) {
            simulationTimeline.play();
            playPauseButton.setText("Pause");
            log("Simulation started");
        } else {
            simulationTimeline.stop();
            playPauseButton.setText("Play");
            log("Simulation paused");
        }
    }

    private void simulationStep() {
        // Allocate some objects automatically
        for (int i = 0; i < 10; i++) {
            try {
                SimulatedObject obj = jvm.getAllocationEngine().allocateRandom();
                if (Math.random() < 0.05) {
                    jvm.getRootSet().addRoot(obj);
                }
            } catch (OutOfMemoryError e) {
                log("Out of memory - triggering GC");
                currentGC.collect();
                break;
            }
        }

        // Trigger GC if needed
        if (currentGC.shouldCollect()) {
            currentGC.collect();
        }

        updateUI();
    }

    private void updateUI() {
        Platform.runLater(() -> {
            // Update progress bars
            updateProgressBar(edenUsageBar, edenLabel, "Eden",
                    jvm.getHeap().getYoungGen().getEden());
            updateProgressBar(survivor0UsageBar, survivor0Label, "Survivor-0",
                    jvm.getHeap().getYoungGen().getSurvivor0());
            updateProgressBar(survivor1UsageBar, survivor1Label, "Survivor-1",
                    jvm.getHeap().getYoungGen().getSurvivor1());
            updateProgressBar(oldGenUsageBar, oldGenLabel, "Old Gen",
                    jvm.getHeap().getOldGen().getTenured());
            updateProgressBar(metaspaceUsageBar, metaspaceLabel, "Metaspace",
                    jvm.getMetaspace());
            updateProgressBar(ramUsageBar, ramLabel, "RAM", jvm.getRam());

            // Update statistics
            totalAllocationsLabel.setText(String.valueOf(jvm.getTotalAllocations()));
            totalGCsLabel.setText(String.valueOf(jvm.getTotalGCs()));
            GCStatistics stats = currentGC.getStatistics();
            lastPauseTimeLabel.setText(stats.getLastPauseTimeMs() + " ms");
            avgPauseTimeLabel.setText(String.format("%.2f ms", stats.getAveragePauseTimeMs()));

            // Update chart
            chartTime++;
            heapSeries.getData().add(new XYChart.Data<>(chartTime, jvm.getHeap().getUsed() / (1024 * 1024)));
            youngGenSeries.getData()
                    .add(new XYChart.Data<>(chartTime, jvm.getHeap().getYoungGen().getUsed() / (1024 * 1024)));
            oldGenSeries.getData()
                    .add(new XYChart.Data<>(chartTime, jvm.getHeap().getOldGen().getUsed() / (1024 * 1024)));

            // Keep chart data manageable
            if (heapSeries.getData().size() > 100) {
                heapSeries.getData().remove(0);
                youngGenSeries.getData().remove(0);
                oldGenSeries.getData().remove(0);
            }
        });
    }

    private void updateProgressBar(ProgressBar bar, Label label, String name,
            com.gcsimulator.model.memory.Memory memory) {
        double utilization = memory.getUtilization();
        bar.setProgress(utilization);

        String text = String.format("%s: %d / %d MB (%.1f%%)",
                name,
                memory.getUsed() / (1024 * 1024),
                memory.getCapacity() / (1024 * 1024),
                utilization * 100);
        label.setText(text);

        // Color code based on usage
        bar.setStyle(utilization > 0.9 ? "-fx-accent: #ff4444;"
                : utilization > 0.7 ? "-fx-accent: #ffaa00;" : "-fx-accent: #44ff44;");
    }

    private void onJVMEvent(JVMSimulator.JVMEvent event) {
        // Log important events
        switch (event.getType()) {
            case GC_STARTED -> log("[GC] " + event.getMessage());
            case GC_COMPLETED -> log("[GC] Completed in " + event.getData() + "ms");
            case OUT_OF_MEMORY -> log("[ERROR] " + event.getMessage());
            case OBJECT_ALLOCATED, OBJECT_COLLECTED, PROMOTION -> {
                // These events are too verbose for the log
            }
        }
    }

    private void log(String message) {
        Platform.runLater(() -> {
            logTextArea.appendText(message + "\n");
        });
    }

    // Menu Item Handlers

    @FXML
    private void handleResetJVM() {
        reset();
    }

    @FXML
    private void handleExit() {
        Platform.exit();
        System.exit(0);
    }

    @FXML
    private void handleClearLog() {
        logTextArea.clear();
        log("Log cleared");
    }

    @FXML
    private void handleAbout() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About GC Simulator");
        alert.setHeaderText("Java Garbage Collection Simulator");
        alert.setContentText(
                "Version: 1.0.0\n\n" +
                        "A comprehensive JavaFX application for simulating and visualizing\n" +
                        "Garbage Collection algorithms across Java versions 8, 17, and 21.\n\n" +
                        "Features 10 GC algorithms:\n" +
                        "• Java 8: Serial, Parallel, CMS, G1\n" +
                        "• Java 17: Enhanced G1, ZGC, Shenandoah\n" +
                        "• Java 21: Latest G1, Generational ZGC, Generational Shenandoah\n\n" +
                        "Built with JavaFX 21.0.1 and Gradle");
        alert.showAndWait();
    }

    @FXML
    private void handleAlgorithmDocumentation() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Algorithm Documentation");
        alert.setHeaderText("GC Algorithm Information");
        alert.setContentText(
                "Detailed documentation for all 10 GC algorithms is available in:\n\n" +
                        "📄 algorithm.md\n\n" +
                        "This file contains comprehensive information about:\n" +
                        "• How each algorithm works\n" +
                        "• Performance characteristics\n" +
                        "• Use cases and recommendations\n" +
                        "• Configuration options\n" +
                        "• Performance comparisons\n\n" +
                        "Location: Project root directory");
        alert.showAndWait();
    }
}
