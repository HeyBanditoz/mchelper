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
        if (!span.getSpanContext().isValid()) {
            return scope;
        }
        // save/restore rather than remove, so closing a nested scope does not strip the trace ID from the outer one
        String previous = MDC.get(MDC_TRACE_ID);
        MDC.put(MDC_TRACE_ID, span.getSpanContext().getTraceId());
        return () -> {
            if (previous == null) {
                MDC.remove(MDC_TRACE_ID);
            }
            else {
                MDC.put(MDC_TRACE_ID, previous);
            }
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
                .put(MCHelperAttributes.MCHELPER_NAME, name)
                .put(MCHelperAttributes.DISCORD_USER_ID, user.getIdLong());
        if (channel != null) {
            builder.put(MCHelperAttributes.DISCORD_CHANNEL_ID, channel.getIdLong());
        }
        if (guild != null) {
            builder.put(MCHelperAttributes.DISCORD_GUILD_ID, guild.getIdLong());
        }
        return builder.build();
    }

    public static void recordStat(Span span, Stat s) {
        span.setAttribute(MCHelperAttributes.MCHELPER_STATUS, s.getStatus().name());
        span.setAttribute(MCHelperAttributes.MCHELPER_KIND, s.getKind().name());
        span.setAttribute(MCHelperAttributes.MCHELPER_EXECUTION_TIME_MS, s.getExecutionTime());
        if (s.getStatus() != Status.SUCCESS) {
            span.setStatus(StatusCode.ERROR, s.getStatus().name());
        }
    }

    public static void recordException(Span span, Throwable t) {
        span.recordException(t);
        span.setStatus(StatusCode.ERROR, t.getClass().getSimpleName());
    }
}
