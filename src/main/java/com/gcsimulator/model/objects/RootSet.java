package com.gcsimulator.model.objects;

import java.util.*;

/**
 * Represents the root set for garbage collection - objects that are always
 * reachable.
 * This includes stack references, static variables, etc.
 */
public class RootSet {
    private final Set<SimulatedObject> roots;
    private final Map<String, SimulatedObject> namedRoots;

    public RootSet() {
        this.roots = new HashSet<>();
        this.namedRoots = new HashMap<>();
    }

    public void addRoot(SimulatedObject obj) {
        roots.add(obj);
    }

    public void addRoot(String name, SimulatedObject obj) {
        roots.add(obj);
        namedRoots.put(name, obj);
    }

    public void removeRoot(SimulatedObject obj) {
        roots.remove(obj);
        namedRoots.values().remove(obj);
    }

    public void removeRoot(String name) {
        SimulatedObject obj = namedRoots.remove(name);
        if (obj != null) {
            roots.remove(obj);
        }
    }

    public Set<SimulatedObject> getRoots() {
        return Collections.unmodifiableSet(roots);
    }

    public SimulatedObject getNamedRoot(String name) {
        return namedRoots.get(name);
    }

    public void clear() {
        roots.clear();
        namedRoots.clear();
    }

    public int size() {
        return roots.size();
    }
}
