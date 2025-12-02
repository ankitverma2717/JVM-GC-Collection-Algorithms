package com.gcsimulator.model.memory;

import java.util.*;

/**
 * Represents a thread stack with stack frames.
 */
public class Stack implements Memory {
    private final String threadName;
    private final long capacity;
    private final Deque<StackFrame> frames;
    private long used;

    public Stack(String threadName, long capacity) {
        this.threadName = threadName;
        this.capacity = capacity;
        this.frames = new ArrayDeque<>();
        this.used = 0;
    }

    public String getThreadName() {
        return threadName;
    }

    public void pushFrame(StackFrame frame) {
        if (used + frame.getSize() > capacity) {
            throw new StackOverflowError("Stack overflow");
        }
        frames.push(frame);
        used += frame.getSize();
    }

    public StackFrame popFrame() {
        if (frames.isEmpty()) {
            return null;
        }
        StackFrame frame = frames.pop();
        used -= frame.getSize();
        return frame;
    }

    public StackFrame peekFrame() {
        return frames.peek();
    }

    public List<StackFrame> getFrames() {
        return new ArrayList<>(frames);
    }

    @Override
    public long getCapacity() {
        return capacity;
    }

    @Override
    public long getUsed() {
        return used;
    }

    @Override
    public void clear() {
        frames.clear();
        used = 0;
    }

    /**
     * Represents a single stack frame (method call).
     */
    public static class StackFrame {
        private final String methodName;
        private final long size;
        private final Set<Object> localReferences;

        public StackFrame(String methodName, long size) {
            this.methodName = methodName;
            this.size = size;
            this.localReferences = new HashSet<>();
        }

        public String getMethodName() {
            return methodName;
        }

        public long getSize() {
            return size;
        }

        public void addLocalReference(Object ref) {
            localReferences.add(ref);
        }

        public Set<Object> getLocalReferences() {
            return localReferences;
        }
    }
}
