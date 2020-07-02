package com.ktully.appd.otel.ui;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.grpc.ManagedChannelBuilder;
import io.opentelemetry.exporters.jaeger.JaegerGrpcSpanExporter;
import io.opentelemetry.exporters.logging.LoggingSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.trace.Samplers;
import io.opentelemetry.sdk.trace.SpanProcessor;
import io.opentelemetry.sdk.trace.config.TraceConfig;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import io.opentelemetry.trace.Tracer;

@Configuration
public class OtelTracerConfig {

	@Bean
	public Tracer OtelTracer() throws Exception {

		final Tracer tracer = OpenTelemetrySdk.getTracerProvider().get("io.opentelemetry.trace.Tracer");

		// Configure the Tracer to grab every span
		TraceConfig alwaysOn = TraceConfig.getDefault().toBuilder().setSampler(Samplers.alwaysOn()).build();
		OpenTelemetrySdk.getTracerProvider().updateActiveTraceConfig(alwaysOn);

		// Jaeger Exporter
		JaegerGrpcSpanExporter jaegerExporter = JaegerGrpcSpanExporter.newBuilder().setServiceName("garagesale-ui")
				.setChannel(ManagedChannelBuilder.forAddress("jaeger", 14250).usePlaintext().build()).build();
		SpanProcessor jaegerProcessor = SimpleSpanProcessor.newBuilder(jaegerExporter).build();

		// Log Exporter
		SpanProcessor logProcessor = SimpleSpanProcessor.newBuilder(new LoggingSpanExporter()).build();

		OpenTelemetrySdk.getTracerProvider().addSpanProcessor(logProcessor);
		OpenTelemetrySdk.getTracerProvider().addSpanProcessor(jaegerProcessor);

		return tracer;

	}

}
