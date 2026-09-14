package io.banditoz.mchelper.telemetry;

import java.io.IOException;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.ContextPropagators;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

@Singleton
public class HttpTracingInterceptor implements Interceptor {
    private final Tracer tracer;
    private final ContextPropagators propagators;

    @Inject
    public HttpTracingInterceptor(OpenTelemetry openTelemetry) {
        this.tracer = openTelemetry.getTracer("io.banditoz.mchelper.http");
        this.propagators = openTelemetry.getPropagators();
    }

    @NotNull
    @Override
    public Response intercept(@NotNull Chain chain) throws IOException {
        Request request = chain.request();
        Span span = tracer.spanBuilder(request.method() + " " + request.url().host())
                .setSpanKind(SpanKind.CLIENT)
                .startSpan();
        if (!span.isRecording()) {
            return chain.proceed(request);
        }
        try (Scope ignored = span.makeCurrent()) {
            span.setAttribute("http.request.method", request.method());
            span.setAttribute("server.address", request.url().host());
            span.setAttribute("url", request.url().toString());
            Request.Builder propagated = request.newBuilder();
            propagators.getTextMapPropagator().inject(Context.current(), propagated, Request.Builder::header);
            Response response = chain.proceed(propagated.build());
            span.setAttribute("http.response.status_code", (long) response.code());
            if (response.code() >= 400) {
                span.setStatus(StatusCode.ERROR, "HTTP " + response.code());
            }
            return response;
        } catch (IOException | RuntimeException e) {
            Tracing.recordException(span, e);
            throw e;
        } finally {
            span.end();
        }
    }
}
