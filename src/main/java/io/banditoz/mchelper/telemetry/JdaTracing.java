package io.banditoz.mchelper.telemetry;

import javax.annotation.Nullable;
import java.util.function.Consumer;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.requests.RestAction;

public class JdaTracing {
    private static volatile Tracer tracer;

    private JdaTracing() {
    }

    public static <T> void queue(RestAction<T> action,
                                 String caller,
                                 @Nullable Consumer<? super T> success,
                                 @Nullable Consumer<? super Throwable> failure) {
        Context parent = Context.current();
        Span span = span(action, caller, "queue").setParent(parent).startSpan();
        if (!span.isRecording()) {
            action.queue(success, failure);
            return;
        }
        Context child = parent.with(span);
        action.queue(t -> {
            try (Scope ignored = child.makeCurrent()) {
                if (success != null) {
                    success.accept(t);
                }
            } finally {
                span.end();
            }
        }, throwable -> {
            try (Scope ignored = child.makeCurrent()) {
                record(span, throwable);
                if (failure != null) {
                    failure.accept(throwable);
                }
                else {
                    RestAction.getDefaultFailure().accept(throwable);
                }
            } finally {
                span.end();
            }
        });
    }

    public static Object complete(RestAction<?> action, String caller, Blocking call) throws Throwable {
        Span span = span(action, caller, "complete").startSpan();
        if (!span.isRecording()) {
            return call.run();
        }
        try (Scope ignored = span.makeCurrent()) {
            return call.run();
        } catch (Throwable t) {
            record(span, t);
            throw t;
        } finally {
            span.end();
        }
    }

    private static SpanBuilder span(RestAction<?> action, String caller, String call) {
        String type = actionType(action);
        return tracer().spanBuilder("discord " + type)
                .setSpanKind(SpanKind.CLIENT)
                .setAttribute("discord.rest_action", type)
                .setAttribute("discord.call", call)
                .setAttribute("code.function", caller);
    }

    private static void record(Span span, Throwable t) {
        Tracing.recordException(span, t);
        if (t instanceof ErrorResponseException ere) {
            span.setAttribute("discord.error_code", (long) ere.getErrorCode());
        }
    }

    private static String actionType(RestAction<?> action) {
        String name = action.getClass().getSimpleName();
        if (name.endsWith("Impl")) {
            name = name.substring(0, name.length() - "Impl".length());
        }
        return name.isEmpty() ? "RestAction" : name;
    }

    private static Tracer tracer() {
        Tracer t = tracer;
        if (t == null) {
            t = GlobalOpenTelemetry.getTracer("io.banditoz.mchelper.jda");
            tracer = t;
        }
        return t;
    }

    @FunctionalInterface
    public interface Blocking {
        Object run() throws Throwable;
    }
}
