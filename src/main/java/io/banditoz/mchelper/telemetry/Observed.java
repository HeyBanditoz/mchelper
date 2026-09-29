package io.banditoz.mchelper.telemetry;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Records the execution time of this method to a histogram, and wraps the invocation in an OTel
 * {@link io.opentelemetry.api.trace.Span} when called within a trace that is being recorded.
 * When placed on a type, every public/public static method of that type is wrapped, excluding those marked
 * {@link SkipAspect}.
 *
 * @apiNote Inspired by <code>io.micrometer.observation.annotation.Observed</code>
 */
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface Observed {
}
