package io.banditoz.mchelper.utils;

import org.reflections.Reflections;

import java.util.Set;

public class ClassUtils {
    private static final Reflections REFLECTIONS = new Reflections("io.banditoz.mchelper");

    /**
     * @param c The {@link Class} to use as the parent class.
     * @param <T> The {@link Class} type.
     * @return A {@link Set} of {@link Class}es that match the parent.
     */
    public static <T> Set<Class<? extends T>> getAllSubtypesOf(Class<T> c) {
        return REFLECTIONS.getSubTypesOf(c);
    }
}
