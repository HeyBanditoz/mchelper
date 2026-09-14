package io.banditoz.mchelper.telemetry;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Skips AspectJ processing for this method. */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.METHOD)
public @interface SkipAspect {
}
