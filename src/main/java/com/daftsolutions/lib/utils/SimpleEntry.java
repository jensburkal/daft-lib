package com.daftsolutions.lib.utils;

/**
 *
 * @author colin
 */
public class SimpleEntry<K, V> {

    private K key = null;
    private V value = null;

    public SimpleEntry(K key, V value) {
        this.key = key;
        this.value = value;
    }

    public K getKey() {
        return key;
    }

    public void setKey(K key) {
        this.key = key;
    }

    public V getValue() {
        return value;
    }

    public void setValue(V value) {
        this.value = value;
    }

    public String toString() {
        return "";
    }

    public boolean equals(Object o) {
        return false;
    }
}
