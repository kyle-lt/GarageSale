package com.ktully.appd.otel.ui;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.grpc.ManagedChannelBuilder;

//0.12.0
import io.opentelemetry.exporter.jaeger.JaegerGrpcSpanExporter;
import io.opentelemetry.exporter.logging.LoggingSpanExporter;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.TracerProvider;
//0.10
//import io.opentelemetry.api.trace.propagation.HttpTraceContext;
//import io.opentelemetry.context.propagation.DefaultContextPropagators;
//0.12.0
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.trace.SpanProcessor;
//0.12.0
//import io.opentelemetry.sdk.trace.TracerSdkManagement;
//0.13.1
import io.opentelemetry.sdk.trace.SdkTracerManagement;
import io.opentelemetry.sdk.trace.config.TraceConfig;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import io.opentelemetry.api.DefaultOpenTelemetry;
//0.13.1
import io.opentelemetry.api.GlobalOpenTelemetry;

@Configuration
public class OtelTracerConfig {

	@Bean
	public Tracer OtelTracer() throws Exception {

		// ** Create Tracer SDK Management and Tracer
		
		// 0.12.0
		//final Tracer tracer = OpenTelemetry.getGlobalTracer("io.opentelemetry.trace.Tracer");
		//final TracerSdkManagement tracerSdkManagement = OpenTelemetrySdk.getGlobalTracerManagement();
		
		// 0.13.1
		// Now, let's instantiate our global tracing config
		//final TracerProvider tracerProvider = TracerProvider.getDefault(); // not truly necessary, I don't think
		//final OpenTelemetry openTelemetry = OpenTelemetrySdk.builder()
		//		.setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance()))
		//		.setTracerProvider(tracerProvider)
		//		.build();
		//final OpenTelemetry openTelemetry = DefaultOpenTelemetry.builder()
		//		.setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance()))
		//		.setTracerProvider(tracerProvider)
		//		.build();
		//GlobalOpenTelemetry.set(openTelemetry);
		//GlobalOpenTelemetry.set(DefaultOpenTelemetry.builder().build());
		
		final OpenTelemetrySdk openTelemetrySdk = OpenTelemetrySdk.builder()
				.setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance()))
				.build();
		// GlobalOpenTelemetry.set(openTelemetrySdk) is called in the build method!
		
		final Tracer tracer = GlobalOpenTelemetry.getTracer("io.opentelemetry.trace.Tracer");
		
		final SdkTracerManagement sdkTracerManagement = OpenTelemetrySdk.getGlobalTracerManagement();
		
		//final OpenTelemetrySdk openTelemetrySdk = OpenTelemetrySdk.get();
		
		// ** Install the W3C Trace Context propagator
		
		// 0.12.0
	    //OpenTelemetry.setGlobalPropagators(
	    //        ContextPropagators.create(W3CTraceContextPropagator.getInstance()));
	    
		// ** Create an alwaysOn TraceConfig
		
	    // 0.12.0
		TraceConfig alwaysOn = TraceConfig.getDefault().toBuilder().setSampler(Sampler.alwaysOn()).build();
		
		// ** Assign the TraceConfig using Trace SDK Management
		
		// 0.12.0
		//tracerSdkManagement.updateActiveTraceConfig(alwaysOn);
		
		// 0.13.1
		sdkTracerManagement.updateActiveTraceConfig(alwaysOn);

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
		//tracerSdkManagement.addSpanProcessor(logProcessor);
		//tracerSdkManagement.addSpanProcessor(jaegerProcessor);
		
		// 0.13.1
		sdkTracerManagement.addSpanProcessor(logProcessor);
		sdkTracerManagement.addSpanProcessor(jaegerProcessor);

		return tracer;

	}

}
