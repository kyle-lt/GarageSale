package com.ktully.appd.otel.ui;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.ktully.appd.otel.ui.Controller.ItemController;
import com.ktully.appd.otel.ui.Model.Item;

//import io.grpc.Context;
// 0.8.0
//import io.opentelemetry.OpenTelemetry;
//import io.opentelemetry.context.propagation.TextMapPropagator;
//import io.opentelemetry.trace.Span;
//import io.opentelemetry.trace.SpanContext;
//import io.opentelemetry.trace.Tracer;

// 0.10.0
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
//import io.opentelemetry.api.trace.propagation.HttpTraceContext;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
//import io.opentelemetry.context.propagation.DefaultContextPropagators;
import io.opentelemetry.context.propagation.TextMapPropagator;
//import io.opentelemetry.exporter.logging.LoggingSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.trace.SpanProcessor;
//import io.opentelemetry.sdk.trace.TracerSdkManagement;
import io.opentelemetry.sdk.trace.config.TraceConfig;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;

/* This class library was built in order to test centralizing the Context propagation injection
 * that is used in the rest of the ui project.  It worked fine, but since the project is simple,
 * I decided to leave the Context propagation in place, at the Controller(s) - most notably ItemController.
 * 
 * Also note, neither the Context propagation here, nor in the Controller, worked when moving from 
 * version 0.8.0 to version 0.9.1.  For some reason, my TextMapPropagator.Setter code was never being
 * executed after the move to 0.9.1.  I suspect that somehow I am not registering the TextMapPropagator, which
 * changed to default to no-op across these versions, but looking at the samples online, I can't tell
 * what I am doing wrong.  It works perfectly in 0.8.0, then magically stops working when I swap out the version
 * to 0.9.1.  Everything else works fine (still collect spans, traces, they just aren't correlated...which is 
 * kind of a big deal).
 * 
 * I am also going to pivot to use the Spring/SpringBoot Instrumentation libraries to see how they
 * work since it's more likely that that'll replace most use cases for manual instrumentation and
 * propagation anyway.
 */

@Component
public class HttpUtils {
	
	private static final Logger logger = LoggerFactory.getLogger(HttpUtils.class);

	private static final TextMapPropagator.Setter<HttpHeaders> setter = new TextMapPropagator.Setter<HttpHeaders>() {
		@Override
		public void set(HttpHeaders headers, String key, String value) {
			logger.debug("RestTemplate - Adding Header with Key = " + key);
			logger.debug("RestTemplate - Adding Header with Value = " + value);
			headers.set(key, value);
		}
	};

	//@Autowired
	//private Tracer tracer;

	private TextMapPropagator textFormat;

	public HttpUtils(Tracer tracer) {
		// textFormat = tracer.getTextMapPropagator();
		// 0.8.0
		//textFormat = OpenTelemetry.getPropagators().getTextMapPropagator();
		// 0.10.0
		textFormat = OpenTelemetry.getGlobalPropagators().getTextMapPropagator();
	}

	public List<Item> callEndpoint(String url) throws Exception {
		HttpHeaders headers = new HttpHeaders();

		try {
			logger.debug("Calling textFormat.inject!");
			textFormat.inject(Context.current(), headers, setter);
			logger.debug("Done calling textFormat.inject!");
			logger.debug("**** Here are the headers: " + headers.toString());
		}
		catch (Exception e) {
			logger.error("Error during OT section, here it is!", e);
		}

		HttpEntity<String> entity = new HttpEntity<String>(headers);
		RestTemplate restTemplate = new RestTemplate();

		// Removing simple string exchange
		// ResponseEntity<String> response =
		// restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

		// Make outgoing call via RestTemplate for List of Items
		ResponseEntity<List<Item>> itemResponse = restTemplate.exchange(url, HttpMethod.GET, entity,
				new ParameterizedTypeReference<List<Item>>() {
				});

		return itemResponse.getBody();
	}

}
