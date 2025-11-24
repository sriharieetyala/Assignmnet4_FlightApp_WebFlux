package com.flightapp.service;

import java.lang.reflect.Field;

/**
 * Small reflection helper I use in tests to inject mocks into private fields.
 * Keeps tests simple when there is no setter available.
 */
public class TestUtils {
    public static void setField(Object target, String name, Object value) {
        try {
            Field f = target.getClass().getDeclaredField(name);
            f.setAccessible(true);
            f.set(target, value);
        } catch (Exception e) {
            // I prefer failing the test clearly so I wrap exceptions into runtime.
            throw new RuntimeException(e);
        }
    }
}