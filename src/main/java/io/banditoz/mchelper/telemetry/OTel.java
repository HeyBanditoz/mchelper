package io.banditoz.mchelper.telemetry;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Duration;

import io.avaje.config.Config;
import io.avaje.inject.Bean;
import io.avaje.inject.Factory;
import io.banditoz.mchelper.Version;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.exporter.otlp.http.trace.OtlpHttpSpanExporter;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.exporter.prometheus.PrometheusHttpServer;
import io.opentelemetry.instrumentation.runtimemetrics.*;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.OpenTelemetrySdkBuilder;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Factory
public class OTel {
    private final OpenTelemetry openTelemetry;
    private static final Logger log = LoggerFactory.getLogger(OTel.class);

    public OTel() {
        boolean metrics = Config.getBool("mchelper.metrics.enabled", false);
        boolean tracing = Config.getBool("mchelper.tracing.enabled", false);
        if (!metrics && !tracing) {
            openTelemetry = OpenTelemetry.noop();
            return;
        }
        Attributes attr = Attributes.builder()
                .put("application", "mchelper")
                .put("hostname", getHostname())
                .build();
        Resource resource = Resource.create(attr);

        OpenTelemetrySdkBuilder builder = OpenTelemetrySdk.builder()
                .setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance()));

        if (metrics) {
            int port = Config.getInt("mchelper.metrics.port", 9092);
            builder.setMeterProvider(SdkMeterProvider.builder()
                    .setResource(resource)
                    .registerMetricReader(PrometheusHttpServer.builder().setPort(port).build())
                    .build());
            log.info("OpenTelemetry metrics built, Prometheus HTTP server listening on {}", port);
        }

        if (tracing) {
            String protocol = Config.get("mchelper.tracing.protocol", "grpc");
            String endpoint = Config.get("mchelper.tracing.endpoint", "http://localhost:4317");
            double ratio = Config.getDecimal("mchelper.tracing.sample-ratio", "1.0").doubleValue();
            builder.setTracerProvider(SdkTracerProvider.builder()
                    .setResource(tracingResource(attr))
                    .setSampler(Sampler.parentBasedBuilder(
                            new RootSpanKindSampler(Sampler.traceIdRatioBased(ratio))).build())
                    .addSpanProcessor(BatchSpanProcessor.builder(spanExporter(protocol, endpoint))
                            .setMaxQueueSize(512)
                            .setMaxExportBatchSize(128)
                            .build())
                    .build());
            log.info("OpenTelemetry tracing built, exporting OTLP/{} to {} at a sample ratio of {}", protocol, endpoint, ratio);
        }
        else {
            builder.setTracerProvider(SdkTracerProvider.builder()
                    .setSampler(Sampler.alwaysOff())
                    .build());
        }

        // registered globally so the woven JdaTracing call sites can reach a tracer
        OpenTelemetrySdk sdk = builder.buildAndRegisterGlobal();

        if (metrics) {
            // can this all be moved to a Java agent per docs?
            MemoryPools.registerObservers(sdk);
            BufferPools.registerObservers(sdk);
            Classes.registerObservers(sdk);
            Cpu.registerObservers(sdk);
            Threads.registerObservers(sdk);
            GarbageCollector.registerObservers(sdk);
        }
        Runtime.getRuntime().addShutdownHook(new Thread(sdk::close));
        this.openTelemetry = sdk;
    }

    @Bean
    public MeterProvider meter() {
        return openTelemetry.getMeterProvider();
    }

    @Bean
    public OpenTelemetry openTelemetry() {
        return openTelemetry;
    }

    @Bean
    public Tracer tracer() {
        return openTelemetry.getTracer("io.banditoz.mchelper");
    }


    private SpanExporter spanExporter(String protocol, String endpoint) {
        Duration timeout = Duration.ofSeconds(Config.getLong("mchelper.tracing.timeout-seconds", 10L));
        return switch (protocol.toLowerCase()) {
            case "grpc" -> OtlpGrpcSpanExporter.builder().setEndpoint(endpoint).setTimeout(timeout).build();
            case "http" -> OtlpHttpSpanExporter.builder().setEndpoint(endpoint).setTimeout(timeout).build();
            default -> throw new IllegalArgumentException(
                    "Unknown mchelper.tracing.protocol \"" + protocol + "\", expected grpc or http");
        };
    }

    private Resource tracingResource(Attributes base) {
        AttributesBuilder b = base.toBuilder()
                .put("service.name", Config.get("mchelper.tracing.service-name", "mchelper"))
                .put("service.version", Version.GIT_SHA);
        Config.getOptional("mchelper.environment")
                .ifPresent(env -> b.put("deployment.environment.name", env));
        return Resource.create(b.build());
    }

    private String getHostname() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }
}
