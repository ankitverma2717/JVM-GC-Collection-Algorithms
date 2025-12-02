package com.gcsimulator.model.objects;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Represents a simulated object in the JVM heap.
 */
public class SimulatedObject {
    private static final AtomicLong ID_GENERATOR = new AtomicLong(0);

    private final long id;
    private final long size;
    private final long creationTime;
    private int age;
    private boolean marked;
    private boolean reachable;
    private String type;
    private final Set<SimulatedObject> references;

    public SimulatedObject(long size, String type) {
        this.id = ID_GENERATOR.incrementAndGet();
        this.size = size;
        this.type = type;
        this.creationTime = System.currentTimeMillis();
        this.age = 0;
        this.marked = false;
        this.reachable = true;
        this.references = new HashSet<>();
    }

    public long getId() {
        return id;
    }

    public long getSize() {
        return size;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public int getAge() {
        return age;
    }

    public void incrementAge() {
        this.age++;
    }

    public void resetAge() {
        this.age = 0;
    }

    public boolean isMarked() {
        return marked;
    }

    public void setMarked(boolean marked) {
        this.marked = marked;
    }

    public boolean isReachable() {
        return reachable;
    }

    public void setReachable(boolean reachable) {
        this.reachable = reachable;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Set<SimulatedObject> getReferences() {
        return references;
    }

    public void addReference(SimulatedObject obj) {
        references.add(obj);
    }

    public void removeReference(SimulatedObject obj) {
        references.remove(obj);
    }

    public void clearReferences() {
        references.clear();
    }

    @Override
    public String toString() {
        return String.format("Object[id=%d, size=%d, age=%d, type=%s]",
                id, size, age, type);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        SimulatedObject that = (SimulatedObject) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
