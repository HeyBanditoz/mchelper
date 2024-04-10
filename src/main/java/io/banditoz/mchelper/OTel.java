package io.banditoz.mchelper;

import io.avaje.config.Config;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.exporter.prometheus.PrometheusHttpServer;
import io.opentelemetry.instrumentation.runtimemetrics.*;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.resources.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class OTel {
    private final OpenTelemetry openTelemetry;
    private static final Logger log = LoggerFactory.getLogger(OTel.class);

    public OTel(boolean enabled) {
        if (!enabled) {
            openTelemetry = OpenTelemetry.noop();
            return;
        }
        Attributes attr = Attributes.builder()
                .put("application", "mchelper")
                .put("hostname", getHostname())
                .build();
        int port = Config.getInt("mchelper.metrics.port", 9092);
        OpenTelemetrySdk openTelemetry = OpenTelemetrySdk.builder()
                .setMeterProvider(
                        SdkMeterProvider.builder()
                                .setResource(Resource.create(attr))
                                .registerMetricReader(
                                        PrometheusHttpServer.builder().setPort(port).build())
                                .build())
                .build();
        // can this all be moved to a Java agent per docs?
        MemoryPools.registerObservers(openTelemetry);
        BufferPools.registerObservers(openTelemetry);
        Classes.registerObservers(openTelemetry);
        Cpu.registerObservers(openTelemetry);
        Threads.registerObservers(openTelemetry);
        GarbageCollector.registerObservers(openTelemetry);
        Runtime.getRuntime().addShutdownHook(new Thread(openTelemetry::close));
        this.openTelemetry = openTelemetry;
        log.info("OpenTelemetry built, Prometheus HTTP server listening on {}", port);
    }

    public MeterProvider meter() {
        return openTelemetry.getMeterProvider();
    }

    public OpenTelemetry openTelemetry() {
        return openTelemetry;
    }

    private String getHostname() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }
}
