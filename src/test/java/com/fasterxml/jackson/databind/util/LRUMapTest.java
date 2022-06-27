package com.fasterxml.jackson.databind.util;

import com.fasterxml.jackson.databind.BaseTest;

public class LRUMapTest extends BaseTest {

    public void testPutGet() {
        LRUMap<String, Integer> m = new LRUMap<>(5, 5);

        assertEquals(0, m.size());
        m.put("k1", 100);
        assertEquals(1, m.size());
        assertNull(m.get("nosuchkey"));
        assertEquals(Integer.valueOf(100), m.get("k1"));

        m.put("k2", 200);
        assertEquals(2, m.size());
        assertEquals(Integer.valueOf(200), m.get("k2"));
    }

    public void testEviction() {
        LRUMap<String, Integer> m = new LRUMap<>(5, 5);

        assertEquals(0, m.size());
        m.put("k1", 100);
        assertEquals(1, m.size());
        m.put("k2", 101);
        assertEquals(2, m.size());
        m.put("k3", 102);
        assertEquals(3, m.size());
        m.put("k4", 103);
        assertEquals(4, m.size());
        m.put("k5", 104);
        assertEquals(5, m.size());
        m.put("k6", 105);
        assertEquals(5, m.size());
        m.put("k7", 106);
        assertEquals(5, m.size());
        m.put("k8", 107);
        assertEquals(5, m.size());

        assertNull(m.get("k3"));
        assertEquals(Integer.valueOf(105), m.get("k6"));
    }
}
