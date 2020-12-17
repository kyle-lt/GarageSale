package com.ktully.appd.otel.ui;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.grpc.ManagedChannelBuilder;

//0.12.0
import io.opentelemetry.exporter.jaeger.JaegerGrpcSpanExporter;
import io.opentelemetry.exporter.logging.LoggingSpanExporter;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Tracer;
//0.10
//import io.opentelemetry.api.trace.propagation.HttpTraceContext;
//import io.opentelemetry.context.propagation.DefaultContextPropagators;
//0.12.0
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.trace.SpanProcessor;
import io.opentelemetry.sdk.trace.TracerSdkManagement;
import io.opentelemetry.sdk.trace.config.TraceConfig;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;

@Configuration
public class OtelTracerConfig {

	@Bean
	public Tracer OtelTracer() throws Exception {

		// ** Create Tracer SDK Management and Tracer
		
		// 0.12.0
		final Tracer tracer = OpenTelemetry.getGlobalTracer("io.opentelemetry.trace.Tracer");
		final TracerSdkManagement tracerSdkManagement = OpenTelemetrySdk.getGlobalTracerManagement();
	    
		// ** Install the W3C Trace Context propagator
		
		// 0.12.0
	    OpenTelemetry.setGlobalPropagators(
	            ContextPropagators.create(W3CTraceContextPropagator.getInstance()));
	    
		// ** Create an alwaysOn TraceConfig
		
	    // 0.12.0
		TraceConfig alwaysOn = TraceConfig.getDefault().toBuilder().setSampler(Sampler.alwaysOn()).build();
		
		// ** Assign the TraceConfig using Trace SDK Management
		
		// 0.12.0
		tracerSdkManagement.updateActiveTraceConfig(alwaysOn);

		// ** Create Exporters
		
		// 0.12.0
		
		// Jaeger Exporter
		JaegerGrpcSpanExporter jaegerExporter = JaegerGrpcSpanExporter.builder().setServiceName("garagesale-ui")
				.setChannel(ManagedChannelBuilder.forAddress("jaeger", 14250).usePlaintext().build()).build();
		SpanProcessor jaegerProcessor = SimpleSpanProcessor.builder(jaegerExporter).build();

		// Log Exporter
		SpanProcessor logProcessor = SimpleSpanProcessor.builder(new LoggingSpanExporter()).build();		

		// ** Add Exporters using Trace SDK Management
		
		// 0.12.0
		tracerSdkManagement.addSpanProcessor(logProcessor);
		tracerSdkManagement.addSpanProcessor(jaegerProcessor);

		return tracer;

	}

}
