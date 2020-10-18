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

import io.grpc.Context;
import io.opentelemetry.OpenTelemetry;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.SpanContext;
import io.opentelemetry.trace.Tracer;

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
		textFormat = OpenTelemetry.getPropagators().getTextMapPropagator();
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
