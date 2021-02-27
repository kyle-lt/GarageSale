package com.ktully.appd.otel.ui;

import java.util.concurrent.TimeUnit;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// 0.13.1
//import io.opentelemetry.exporter.jaeger.JaegerGrpcSpanExporter;
import io.opentelemetry.exporter.logging.LoggingSpanExporter;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;

// 0.14.1
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.trace.SdkTracerProvider;

// OTLP Exporter
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;

@Configuration
public class OtelTracerConfig {

	@Bean
	//public Tracer OtelTracer() throws Exception {
	public static OpenTelemetry OpenTelemetryConfig() {
		
		// ** General Comments **
		// Re-writing a lot of this code for changes put into effect for 0.14.1
		// The original class returned a Tracer, but this time, we return the OpenTelemetrySDK
		
		// ** Create Jaeger Exporter **
		// COMMENTING OUT THE JAEGER EXPORTER, PUTTING IN PLACE THE OTLP COLLECTOR
		//JaegerGrpcSpanExporter jaegerExporter = JaegerGrpcSpanExporter.builder().setServiceName("garagesale-ui")
		//		.setChannel(ManagedChannelBuilder.forAddress("jaeger", 14250).usePlaintext().build()).build();
		
	    // ** Create OTLP gRPC Span Exporter & BatchSpanProcessor **
		OtlpGrpcSpanExporter spanExporter =
	            OtlpGrpcSpanExporter.builder()
	            	.setEndpoint("http://host.docker.internal:4317")
	            	.setTimeout(2, TimeUnit.SECONDS).build();
	        BatchSpanProcessor spanProcessor =
	            BatchSpanProcessor.builder(spanExporter)
	                .setScheduleDelay(100, TimeUnit.MILLISECONDS)
	                .build();	   
	        
		// This was working using OTEL_RESOURCE_ATTRIBUTES env var, but apparently not anymore with 0.15.0
	    AttributeKey<String> myServiceName = AttributeKey.stringKey("service.name");
		AttributeKey<String> myServiceNamespace = AttributeKey.stringKey("service.namespace");	        
	    Resource serviceNameResource =
	                Resource.create(Attributes.of(myServiceName, "garagesale-ui",
	                		myServiceNamespace, "kjt-OTel-GarageSale"));
		
		// ** Create OpenTelemetry SdkTracerProvider
		// Use OTLP & Logging Exporters
	    // Use Service Name Resource (and attributes) defined above
		// Use AlwaysOn TraceConfig
        SdkTracerProvider sdkTracerProvider =
                SdkTracerProvider.builder()
                    .addSpanProcessor(spanProcessor) // OTLP
                    .addSpanProcessor(SimpleSpanProcessor.create(new LoggingSpanExporter()))
                    .setResource(Resource.getDefault().merge(serviceNameResource))
                    .setSampler(Sampler.alwaysOn())
                    .build();
	        
	    // ** Create OpenTelemetry SDK **
		// Use W3C Trace Context Propagation
        // Use the SdkTracerProvider instantiated above
	    OpenTelemetrySdk openTelemetrySdk =
	            OpenTelemetrySdk.builder()
                	.setTracerProvider(sdkTracerProvider)
	            	.setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance()))
	                .build();
	    			//.buildAndRegisterGlobal();  // can/should I use this?  Maybe later...
	    
	    // ** Create Tracer **
	    // Tracers are now being instantiated in each Controller - although I'd like to go back
	    // to this sort of "global" Tracer instantiation - maybe later...
	    // We are grabbing it from the OpenTelemetrySDK
	    //final Tracer tracer = openTelemetrySdk.getTracer("io.opentelemetry.trace.Tracer");
	    
	    //  ** Create Shutdown Hook **
	    Runtime.getRuntime().addShutdownHook(new Thread(sdkTracerProvider::shutdown));
	    
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
