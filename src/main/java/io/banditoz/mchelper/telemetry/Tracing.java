package io.banditoz.mchelper.telemetry;

import javax.annotation.Nullable;

import io.banditoz.mchelper.stats.Stat;
import io.banditoz.mchelper.stats.Status;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.*;
import io.opentelemetry.context.Scope;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.Channel;
import org.slf4j.MDC;

@Singleton
public class Tracing {
    public static final String MDC_TRACE_ID = "traceId";
    private final Tracer tracer;

    @Inject
    public Tracing(Tracer tracer) {
        this.tracer = tracer;
    }

    public Span startRootSpan(String name, Attributes attributes) {
        return tracer.spanBuilder(name)
                .setSpanKind(SpanKind.CONSUMER)
                .setNoParent()
                .setAllAttributes(attributes)
                .startSpan();
    }

    public static Scope makeCurrent(Span span) {
        Scope scope = span.makeCurrent();
        if (span.getSpanContext().isValid()) {
            MDC.put(MDC_TRACE_ID, span.getSpanContext().getTraceId());
        }
        return () -> {
            MDC.remove(MDC_TRACE_ID);
            scope.close();
        };
    }

    /**
     * @return The trace ID of the span currently in context, or null if there is none.
     */
    @Nullable
    public static String currentTraceId() {
        SpanContext context = Span.current().getSpanContext();
        return context.isValid() ? context.getTraceId() : null;
    }

    public static Attributes discordAttributes(String name, User user, @Nullable Channel channel, @Nullable Guild guild) {
        var builder = Attributes.builder()
                .put("mchelper.name", name)
                .put("discord.user_id", user.getIdLong());
        if (channel != null) {
            builder.put("discord.channel_id", channel.getIdLong());
        }
        if (guild != null) {
            builder.put("discord.guild_id", guild.getIdLong());
        }
        return builder.build();
    }

    public static void recordStat(Span span, Stat s) {
        span.setAttribute("mchelper.status", s.getStatus().name());
        span.setAttribute("mchelper.kind", s.getKind().name());
        span.setAttribute("mchelper.execution_time_ms", s.getExecutionTime());
        if (s.getStatus() != Status.SUCCESS) {
            span.setStatus(StatusCode.ERROR, s.getStatus().name());
        }
    }

    public static void recordException(Span span, Throwable t) {
        span.recordException(t);
        span.setStatus(StatusCode.ERROR, t.getClass().getSimpleName());
    }
}
