package com.lach.common.util;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class WeakValueHashMap<K, V> implements Map<K, V> {
    private Map<K, WeakReference<V>> mMap;

    public WeakValueHashMap() {
        mMap = new HashMap<>();
    }

    @Nullable
    @Override
    public V get(Object key) {
        WeakReference<V> objRef = mMap.get(key);
        if (objRef == null) {
            return null;
        }

        V value = objRef.get();
        if (value == null) {
            // Ensure the key is removed if the object was released.
            mMap.remove(key);
        }
        return value;
    }

    @Override
    public V put(K key, V value) {
        mMap.put(key, new WeakReference<>(value));
        return value;
    }

    @Override
    public void clear() {
        mMap.clear();
    }

    @Override
    public boolean containsKey(Object key) {
        return mMap.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return mMap.containsValue(value);
    }

    @NonNull
    @Override
    public Set<Entry<K, V>> entrySet() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isEmpty() {
        return mMap.isEmpty();
    }

    @NonNull
    @Override
    public Set<K> keySet() {
        return mMap.keySet();
    }

    @Override
    public void putAll(@NonNull Map<? extends K, ? extends V> map) {
        throw new UnsupportedOperationException();
    }

    @Override
    public V remove(Object key) {
        WeakReference<V> remove = mMap.remove(key);
        if (remove == null) {
            return null;
        }
        return remove.get();
    }

    @Override
    public int size() {
        return mMap.size();
    }

    @NonNull
    @Override
    public Collection<V> values() {
        throw new UnsupportedOperationException();
    }
}