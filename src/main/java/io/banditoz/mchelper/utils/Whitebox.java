/*
 * Copyright (c) 2007 Mockito contributors
 * This program is made available under the terms of the MIT License.
 */
package io.banditoz.mchelper.utils;

import java.lang.reflect.Field;

/**
 * Class to facilitate getting/setting private and/or final fields in an object.<br>
 * This class was removed from the Mockito library.
 *
 * @author <a href="https://github.com/mockito/mockito/blob/fea4074f8d4a7b1f7b8e89d4819bef25b1b6228f/src/main/java/org/mockito/internal/util/reflection/Whitebox.java">Mockito source file</a>
 */
public class Whitebox {
    /**
     * Gets a private/final field of an object.
     *
     * @param target The target object to fetch from.
     * @param field  The name of the field to fetch.
     * @return The {@link Object} from the internal state.
     */
    public static Object getInternalState(Object target, String field) {
        Class<?> c = target.getClass();
        try {
            Field f = getFieldFromHierarchy(c, field);
            f.setAccessible(true);
            return f.get(target);
        } catch (Exception e) {
            throw new RuntimeException("Unable to get internal state on a private field.", e);
        }
    }

    /**
     * Sets a private/final field of an object.
     *
     * @param target The target object to manipulate.
     * @param field  The name of the field to manipulate.
     * @param value  The value to set.
     */
    public static void setInternalState(Object target, String field, Object value) {
        Class<?> c = target.getClass();
        try {
            Field f = getFieldFromHierarchy(c, field);
            f.setAccessible(true);
            f.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException("Unable to set internal state on a private field.", e);
        }
    }

    private static Field getFieldFromHierarchy(Class<?> clazz, String field) {
        Field f = getField(clazz, field);
        while (f == null && clazz != Object.class) {
            clazz = clazz.getSuperclass();
            f = getField(clazz, field);
        }
        if (f == null) {
            throw new RuntimeException(
                    "You want me to get this field: '" + field +
                            "' on this class: '" + clazz.getSimpleName() +
                            "' but this field is not declared within the hierarchy of this class!");
        }
        return f;
    }

    private static Field getField(Class<?> clazz, String field) {
        try {
            return clazz.getDeclaredField(field);
        } catch (NoSuchFieldException e) {
            return null;
        }
    }
}