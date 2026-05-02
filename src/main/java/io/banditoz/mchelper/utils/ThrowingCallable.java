package io.banditoz.mchelper.utils;

@FunctionalInterface
public interface ThrowingCallable<T> {
    T call() throws Throwable;
}
