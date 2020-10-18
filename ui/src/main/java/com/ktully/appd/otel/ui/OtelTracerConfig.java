package com.ktully.appd.otel.ui;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.grpc.ManagedChannelBuilder;
import io.opentelemetry.OpenTelemetry;
import io.opentelemetry.exporters.jaeger.JaegerGrpcSpanExporter;
import io.opentelemetry.exporters.logging.LoggingSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.trace.Samplers;
import io.opentelemetry.sdk.trace.SpanProcessor;
import io.opentelemetry.sdk.trace.config.TraceConfig;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import io.opentelemetry.trace.Tracer;
//import io.opentelemetry.sdk.trace.TracerSdkManagement;

@Configuration
public class OtelTracerConfig {

	@Bean
	public Tracer OtelTracer() throws Exception {

		// ** Create Tracer SDK Management and Tracer
		
		// 0.8.0
		//final Tracer tracer = OpenTelemetrySdk.getTracerProvider().get("io.opentelemetry.trace.Tracer");
		// 0.8.0 Testing
		final Tracer tracer = OpenTelemetry.getTracer("io.opentelemetry.trace.Tracer");
		
		// 0.9.1
		//final TracerSdkManagement tracerSdkManagement = OpenTelemetrySdk.getTracerManagement();
		//final Tracer tracer = OpenTelemetry.getTracer("io.opentelemetry.trace.Tracer");

		// ** Create an alwaysOn TraceConfig
		
		// Configure the Tracer to grab every span
		TraceConfig alwaysOn = TraceConfig.getDefault().toBuilder().setSampler(Samplers.alwaysOn()).build();
		
		// ** Assign the TraceConfig using Trace SDK Management
		
		// 0.8.0
		OpenTelemetrySdk.getTracerProvider().updateActiveTraceConfig(alwaysOn);
		
		// 0.9.1
		//tracerSdkManagement.updateActiveTraceConfig(alwaysOn);

		// ** Create Exporters
		
		// Jaeger Exporter
		JaegerGrpcSpanExporter jaegerExporter = JaegerGrpcSpanExporter.newBuilder().setServiceName("garagesale-ui")
				.setChannel(ManagedChannelBuilder.forAddress("jaeger", 14250).usePlaintext().build()).build();
		SpanProcessor jaegerProcessor = SimpleSpanProcessor.newBuilder(jaegerExporter).build();

		// Log Exporter
		SpanProcessor logProcessor = SimpleSpanProcessor.newBuilder(new LoggingSpanExporter()).build();

		// ** Add Exporters using Trace SDK Management
		
		// 0.8.0
		OpenTelemetrySdk.getTracerProvider().addSpanProcessor(logProcessor);
		OpenTelemetrySdk.getTracerProvider().addSpanProcessor(jaegerProcessor);
		
		// 0.9.1
		//tracerSdkManagement.addSpanProcessor(logProcessor);
		//tracerSdkManagement.addSpanProcessor(jaegerProcessor);

		return tracer;

	}

}
