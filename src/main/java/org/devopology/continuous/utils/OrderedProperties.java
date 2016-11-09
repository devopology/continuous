package org.devopology.continuous.utils;

import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;

public class OrderedProperties extends Properties {

    private LinkedHashSet<Object> linkedHashSet = new LinkedHashSet<Object>();

    public Object put(Object key, Object value) {
        if (!linkedHashSet.contains(key)) {
            linkedHashSet.add(key);
        }
        return super.put(key, value);
    }

    public void putAll(Properties properties) {
        for (Object key : properties.keySet()) {
            put(key, properties.get(key));
        }
    }

    public Set<Object> keySet() {
        return linkedHashSet;
    }
}