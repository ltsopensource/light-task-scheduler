package com.github.ltsopensource.core.commons.utils;

import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Light-weight object pool based on a thread-local stack.
 *
 * @param <T> the type of the pooled object
 */
public abstract class Recycler<T> {

    private final ThreadLocal<Stack<T>> threadLocal = new ThreadLocal<Stack<T>>() {
        @Override
        protected Stack<T> initialValue() {
            return new Stack<T>(Recycler.this, Thread.currentThread());
        }
    };

    public final T get() {
        Stack<T> stack = threadLocal.get();
        T o = stack.pop();
        if (o == null) {
            o = newObject(stack);
        }
        return o;
    }

    public final boolean recycle(T o, Handle handle) {
        @SuppressWarnings("unchecked")
        Stack<T> stack = (Stack<T>) handle;
        if (stack.parent != this) {
            return false;
        }

        if (Thread.currentThread() != stack.thread) {
            return false;
        }

        stack.push(o);
        return true;
    }

    protected abstract T newObject(Handle handle);

    public interface Handle {
    }

    static final class Stack<T> implements Handle {

        private static final int INITIAL_CAPACITY = 256;

        final Recycler<T> parent;
        final Thread thread;
        private T[] elements;
        private int size;
        private final Map<T, Boolean> map = new IdentityHashMap<T, Boolean>(INITIAL_CAPACITY);

        @SuppressWarnings({"unchecked", "SuspiciousArrayCast"})
        Stack(Recycler<T> parent, Thread thread) {
            this.parent = parent;
            this.thread = thread;
            elements = newArray(INITIAL_CAPACITY);
        }

        T pop() {
            int size = this.size;
            if (size == 0) {
                return null;
            }
            size--;
            T ret = elements[size];
            elements[size] = null;
            map.remove(ret);
            this.size = size;
            return ret;
        }

        void push(T o) {
            if (map.put(o, Boolean.TRUE) != null) {
                throw new IllegalStateException("recycled already");
            }

            int size = this.size;
            if (size == elements.length) {
                T[] newElements = newArray(size << 1);
                System.arraycopy(elements, 0, newElements, 0, size);
                elements = newElements;
            }

            elements[size] = o;
            this.size = size + 1;
        }

        @SuppressWarnings({"unchecked", "SuspiciousArrayCast"})
        private static <T> T[] newArray(int length) {
            return (T[]) new Object[length];
        }
    }
}

