package io.banditoz.mchelper.telemetry;

import java.util.List;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.DoubleHistogram;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import io.opentelemetry.semconv.CodeAttributes;
import io.opentelemetry.semconv.ErrorAttributes;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

@Aspect
public class ObservedAspect {
    private static volatile Tracer tracer;
    private static volatile DoubleHistogram duration;

    @Around("""
            (execution(@io.banditoz.mchelper.telemetry.Observed * *(..))
             || (execution(public * *(..)) && within(@io.banditoz.mchelper.telemetry.Observed *)))
             && !execution(@io.banditoz.mchelper.telemetry.SkipAspect * *(..))""")
    public Object aroundObserved(ProceedingJoinPoint pjp) throws Throwable {
        Signature sig = pjp.getSignature();
        Attributes attributes = Attributes.of(CodeAttributes.CODE_FUNCTION_NAME, sig.getDeclaringTypeName() + '.' + sig.getName());
        Span span = Span.current().isRecording() ? startSpan(sig, attributes) : null;
        long start = System.nanoTime();
        try (Scope _ = span != null ? Tracing.makeCurrent(span) : null) {
            return pjp.proceed();
        }
        catch (Throwable t) {
            if (span != null) {
                Tracing.recordException(span, t);
            }
            attributes = attributes.toBuilder().put(ErrorAttributes.ERROR_TYPE, t.getClass().getName()).build();
            throw t;
        }
        finally {
            duration().record((System.nanoTime() - start) / 1_000_000d, attributes);
            if (span != null) {
                span.end();
            }
        }
    }

    private static Span startSpan(Signature sig, Attributes attributes) {
        return tracer().spanBuilder(sig.getDeclaringType().getSimpleName() + '#' + sig.getName())
                .setSpanKind(SpanKind.INTERNAL)
                .setAllAttributes(attributes)
                .startSpan();
    }

    // TODO roll this into JdaTracing's tracer?
    private static Tracer tracer() {
        Tracer t = tracer;
        if (t == null) {
            t = GlobalOpenTelemetry.getTracer("io.banditoz.mchelper.observed");
            tracer = t;
        }
        return t;
    }

    private static DoubleHistogram duration() {
        DoubleHistogram h = duration;
        if (h == null) {
            h = GlobalOpenTelemetry.getMeter("io.banditoz.mchelper.observed")
                    .histogramBuilder("mchelper_method_duration_milliseconds")
                    .setDescription("Histogram tracking the execution time of methods marked @Observed.")
                    .setExplicitBucketBoundariesAdvice(List.of(0.05, 0.1, 0.25, 0.5, 1d, 2.5, 5d, 10d, 25d, 50d, 100d, 250d, 500d, 1000d, 2500d, 5000d, 10000d))
                    .build();
            duration = h;
        }
        return h;
    }
}
