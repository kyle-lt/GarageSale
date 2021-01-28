package com.ktully.appd.otel.ui;

import java.util.concurrent.TimeUnit;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.grpc.ManagedChannelBuilder;

// 0.13.1
import io.opentelemetry.exporter.jaeger.JaegerGrpcSpanExporter;
import io.opentelemetry.exporter.logging.LoggingSpanExporter;
//import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.sdk.OpenTelemetrySdk;
//import io.opentelemetry.sdk.trace.SpanProcessor;
//import io.opentelemetry.sdk.trace.SdkTracerManagement;
import io.opentelemetry.sdk.trace.config.TraceConfig;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
//import io.opentelemetry.api.GlobalOpenTelemetry;

// 0.14.1
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.sdk.trace.SdkTracerProvider;

// OTLP Exporter
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;


@Configuration
public class OtelTracerConfig {

	@Bean
	//public Tracer OtelTracer() throws Exception {
	public static OpenTelemetry OpenTelemetryConfig() {
		
		
		// Re-writing a lot of this code for changes put into effect for 0.14.1
			// The original class returned a Tracer, but this time, we return the OpenTelemetrySDK
		
		// COMMENTING OUT THE JAEGER EXPORTER, PUTTING IN PLACE THE OTLP COLLECTOR
		// ** Create Jaeger Exporter **
		//JaegerGrpcSpanExporter jaegerExporter = JaegerGrpcSpanExporter.builder().setServiceName("garagesale-ui")
		//		.setChannel(ManagedChannelBuilder.forAddress("jaeger", 14250).usePlaintext().build()).build();
		
	    OtlpGrpcSpanExporter spanExporter =
	            OtlpGrpcSpanExporter.builder()
	            	.setEndpoint("host.docker.internal:4317")
	            	.setTimeout(2, TimeUnit.SECONDS).build();
	        BatchSpanProcessor spanProcessor =
	            BatchSpanProcessor.builder(spanExporter)
	                .setScheduleDelay(100, TimeUnit.MILLISECONDS)
	                .build();
	        
	   
		
		// ** Create OpenTelemetry SDK **
		// Use W3C Trace Context Propagation
		// Use Jaeger & Logging Exporters
		// Use AlwaysOn TraceConfig
	    OpenTelemetrySdk openTelemetrySdk =
	            OpenTelemetrySdk.builder()
	            	.setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance()))
	                .setTracerProvider(
	                    SdkTracerProvider.builder()
	                        //.addSpanProcessor(SimpleSpanProcessor.create(jaegerExporter)) // REMOVING JAEGER FOR OTLP
	                    	.addSpanProcessor(spanProcessor)
	                        .addSpanProcessor(SimpleSpanProcessor.create(new LoggingSpanExporter()))
	                        .setTraceConfig(TraceConfig.builder().setSampler(Sampler.alwaysOn()).build())    
	                        .build())
	                .build();
	    			//.buildAndRegisterGlobal();  // can/should I use this?
		
	    // ** Create Tracer **
	    // We are grabbing it from the OpenTelemetrySDK
	    //final Tracer tracer = openTelemetrySdk.getTracer("io.opentelemetry.trace.Tracer");
	    
	    //  ** Create Shutdown Hook **
	    Runtime.getRuntime()
        .addShutdownHook(new Thread(() -> openTelemetrySdk.getTracerManagement().shutdown()));
	    
	    return openTelemetrySdk;
	    
	    /*

		// ** Create OpenTelemetrySdk which acts as the Global OTel Instance
		// Note: GlobalOpenTelemetry.set(openTelemetrySdk) is called in the build method!
		
		final OpenTelemetrySdk openTelemetrySdk = OpenTelemetrySdk.builder()
				.setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance()))
				.build();
		
		// ** Create Tracer, SdkTracerManagement to add things like AlwaysOn TraceConfig and Exporters
		
		final Tracer tracer = GlobalOpenTelemetry.getTracer("io.opentelemetry.trace.Tracer");
		
		final SdkTracerManagement sdkTracerManagement = OpenTelemetrySdk.getGlobalTracerManagement();
	    
		// ** Create an alwaysOn TraceConfig
		
	    // 0.13.1
		TraceConfig alwaysOn = TraceConfig.getDefault().toBuilder().setSampler(Sampler.alwaysOn()).build();
		
		// ** Assign the TraceConfig using Trace SDK Management
		
		// 0.13.1
		sdkTracerManagement.updateActiveTraceConfig(alwaysOn);

		// ** Create Exporters
		
		// 0.13.1
		
		// Jaeger Exporter
		JaegerGrpcSpanExporter jaegerExporter = JaegerGrpcSpanExporter.builder().setServiceName("garagesale-ui")
				.setChannel(ManagedChannelBuilder.forAddress("jaeger", 14250).usePlaintext().build()).build();
		SpanProcessor jaegerProcessor = SimpleSpanProcessor.builder(jaegerExporter).build();

		// Log Exporter
		SpanProcessor logProcessor = SimpleSpanProcessor.builder(new LoggingSpanExporter()).build();		

		// ** Add Exporters using Trace SDK Management
		
		// 0.13.1
		sdkTracerManagement.addSpanProcessor(logProcessor);
		sdkTracerManagement.addSpanProcessor(jaegerProcessor);

		return tracer;
		
		*/

	}

}
