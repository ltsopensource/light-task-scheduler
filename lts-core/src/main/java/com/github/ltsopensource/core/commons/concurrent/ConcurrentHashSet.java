package com.github.ltsopensource.core.commons.concurrent;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Robert HG (254963746@qq.com) on 8/17/14.
 */
public class ConcurrentHashSet<E> extends AbstractSet<E> implements Set<E> {

    private transient ConcurrentHashMap<E, Object> map;

    // Dummy value to associate with an Object in the backing Map
    private static final Object PRESENT = new Object();

    public ConcurrentHashSet() {
        map = new ConcurrentHashMap<E, Object>();
    }

    public ConcurrentHashSet(Collection<? extends E> c) {
        map = new ConcurrentHashMap<E, Object>(Math.max((int) (c.size() / .75f) + 1, 16));
        addAll(c);
    }

    public ConcurrentHashSet(int initialCapacity, float loadFactor) {
        map = new ConcurrentHashMap<E, Object>(initialCapacity, loadFactor);
    }

    public ConcurrentHashSet(int initialCapacity) {
        map = new ConcurrentHashMap<E, Object>(initialCapacity);
    }

    ConcurrentHashSet(int initialCapacity, float loadFactor, boolean dummy) {
        map = new ConcurrentHashMap<E, Object>(initialCapacity, loadFactor);
    }

    public Set<E> list() {
        return map.keySet();
    }

    public Iterator<E> iterator() {
        return map.keySet().iterator();
    }

    public int size() {
        return map.size();
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }

    public boolean contains(Object o) {
        return map.containsKey(o);
    }

    public boolean add(E e) {
        return map.put(e, PRESENT) == null;
    }

    public boolean remove(Object o) {
        return map.remove(o) == PRESENT;
    }

    public void clear() {
        map.clear();
    }


}