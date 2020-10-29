package com.ktully.appd.otel.itemapi.Controller;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.ktully.appd.otel.itemapi.Model.ItemModel;
import com.ktully.appd.otel.itemapi.Service.ItemService;

import io.grpc.Context;
import io.opentelemetry.OpenTelemetry;
import io.opentelemetry.context.ContextUtils;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.trace.Tracer;
import io.opentelemetry.trace.TracingContextUtils;
import io.opentelemetry.trace.Span;

@RestController
public class ItemController {

	private static final Logger logger = LoggerFactory.getLogger(ItemController.class);

	@Autowired
	ItemService itemService;

	@Autowired
	Tracer tracer;

	/*
	 * Configuration for Context Propagation to be done via @RequestHeader
	 * extraction
	 */
	TextMapPropagator.Getter<Map<String, String>> getter = new TextMapPropagator.Getter<Map<String, String>>() {
		@Override
		public String get(Map<String, String> carrier, String key) {
			logger.debug("Key = " + key);
			logger.debug("Key found! " + key);
			logger.debug("Value = " + carrier.get(key));
			return carrier.get(key);
		}
	};

	/*
	 * Configuration for Context Propagation to be done via HttpHeaders injection
	 */
	private static final TextMapPropagator.Setter<HttpHeaders> httpHeadersSetter = new TextMapPropagator.Setter<HttpHeaders>() {
		@Override
		public void set(HttpHeaders carrier, String key, String value) {
			logger.debug("RestTemplate - Adding Header with Key = " + key);
			logger.debug("RestTemplate - Adding Header with Value = " + value);
			carrier.set(key, value);
		}
	};

	@GetMapping("/items")
	// private List<ItemModel> getAllItems() {
	private List<ItemModel> getAllItems(@RequestHeader Map<String, String> headers) {

		// debug
		// headers.forEach((k,v) -> logger.debug("Key = " + k + ", Value = " = v));
		for (Map.Entry<String, String> entry : headers.entrySet())
			System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue());

		Context extractedContext = null;
		try {
			logger.debug("Trying to extact Context Propagation Headers");
			extractedContext = OpenTelemetry.getPropagators().getTextMapPropagator().extract(Context.current(), headers,
					getter);
			logger.debug(extractedContext.toString());
		} catch (Exception e) {
			logger.error("Exception caught while extracting Context Propagators", e);
		}

		Span serverSpan = null;
		try (Scope scope = ContextUtils.withScopedContext(extractedContext)) {
			// Automatically use the extracted SpanContext as parent.
			logger.debug("Trying to build Span and then make DB call.");
			serverSpan = tracer.spanBuilder("/item-api/items").setSpanKind(Span.Kind.SERVER).startSpan();
			// Add the attributes defined in the Semantic Conventions
			serverSpan.setAttribute("http.method", "GET");
			serverSpan.setAttribute("http.scheme", "http");
			serverSpan.setAttribute("http.host", "item-api:8081");
			serverSpan.setAttribute("http.target", "/items");

			List<ItemModel> items = null;
			try (Scope itemServiceScope = tracer.withSpan(serverSpan)) {
				items = itemService.getAllItems();
			}
			return items;
		} catch (Exception e) {
			logger.error("Exception caught attempting to create Span", e);
			return itemService.getAllItems();
		} finally {
			if (serverSpan != null) {
				serverSpan.end();
			}
		}

	}

	@GetMapping("/item/{id}")
	private ItemModel getItem(@PathVariable("id") int id, @RequestHeader Map<String, String> headers) {

		Context extractedContext = null;
		try {
			logger.debug("Trying to extact Context Propagation Headers");
			extractedContext = OpenTelemetry.getPropagators().getTextMapPropagator().extract(Context.current(), headers,
					getter);
		} catch (Exception e) {
			logger.error("Exception caught while extracting Context Propagators", e);
		}

		Span serverSpan = null;
		try (Scope scope = ContextUtils.withScopedContext(extractedContext)) {
			// Automatically use the extracted SpanContext as parent.
			logger.debug("Trying to build Span and then make DB call.");
			serverSpan = tracer.spanBuilder("/item-api/item/{id}").setSpanKind(Span.Kind.SERVER).startSpan();
			// Add the attributes defined in the Semantic Conventions
			serverSpan.setAttribute("http.method", "GET");
			serverSpan.setAttribute("http.scheme", "http");
			serverSpan.setAttribute("http.host", "item-api:8081");
			serverSpan.setAttribute("http.target", "/item/" + id);

			return itemService.getItemById(id);

		} catch (Exception e) {
			logger.error("Exception caught attempting to create Span", e);
			return itemService.getItemById(id);
		} finally {
			if (serverSpan != null) {
				serverSpan.end();
			}
		}

	}

	@DeleteMapping("/item/{id}")
	private void deleteItem(@PathVariable("id") int id) {
		itemService.delete(id);
	}

	@PostMapping("/item")
	private int saveItem(@RequestBody ItemModel itemModel, @RequestHeader Map<String, String> headers) {

		Context extractedContext = null;
		try {
			logger.debug("Trying to extact Context Propagation Headers");
			extractedContext = OpenTelemetry.getPropagators().getTextMapPropagator().extract(Context.current(), headers,
					getter);
		} catch (Exception e) {
			logger.error("Exception caught while extracting Context Propagators", e);
		}

		Span serverSpan = null;
		try (Scope scope = ContextUtils.withScopedContext(extractedContext)) {
			// Automatically use the extracted SpanContext as parent.
			logger.debug("Trying to build Span and then make DB call.");
			serverSpan = tracer.spanBuilder("/item-api/item").setSpanKind(Span.Kind.SERVER).startSpan();
			// Add the attributes defined in the Semantic Conventions
			serverSpan.setAttribute("http.method", "POST");
			serverSpan.setAttribute("http.scheme", "http");
			serverSpan.setAttribute("http.host", "item-api:8081");
			serverSpan.setAttribute("http.target", "/item");

			return itemService.saveOrUpdate(itemModel).getId();
		} catch (Exception e) {
			logger.error("Exception caught attempting to create Span", e);
			return itemService.saveOrUpdate(itemModel).getId();
		} finally {
			if (serverSpan != null) {
				serverSpan.end();
			}
		}

	}

	@GetMapping("/distribute")
	private String distribute(@RequestHeader Map<String, String> headers) {

		// debug
		// headers.forEach((k,v) -> logger.debug("Key = " + k + ", Value = " = v));
		logger.debug("Incoming Request Headers from ui to item-api/distribute:");
		for (Map.Entry<String, String> entry : headers.entrySet()) {
			System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue());
			logger.debug("Key = " + entry.getKey() + ", Value = " + entry.getValue());
		}
		
		Context extractedContext = null;
		try {
			logger.debug("Trying to extact Context Propagation Headers from ui to item-api/distribute.");
			extractedContext = OpenTelemetry.getPropagators().getTextMapPropagator().extract(Context.current(), headers,
					getter);
			logger.debug(extractedContext.toString());
		} catch (Exception e) {
			logger.error("Exception caught while extracting Context Propagators", e);
		}

		Span serverSpan = null;
		try (Scope scope = ContextUtils.withScopedContext(extractedContext)) {
			// Automatically use the extracted SpanContext as parent.
			logger.debug("Trying to build Span and then make RestTemplate call to .NET Core App.");
			serverSpan = tracer.spanBuilder("/distribute").setSpanKind(Span.Kind.SERVER).startSpan();
			// Add the attributes defined in the Semantic Conventions
			serverSpan.setAttribute("http.method", "GET");
			serverSpan.setAttribute("http.scheme", "http");
			serverSpan.setAttribute("http.host", "item-api:8081");
			serverSpan.setAttribute("http.target", "/distribute");

			RestTemplate restTemplate = new RestTemplate();
			HttpHeaders propagationHeaders = new HttpHeaders();

			// Start a Span for (and send) RestTemplate
			//Span restTemplateSpan = tracer.spanBuilder("/todomvcui/Home/ToDo").setSpanKind(Span.Kind.CLIENT)
			//		.startSpan();
			Span restTemplateSpan = tracer.spanBuilder("/todomvcui/Home/ToDo").setSpanKind(Span.Kind.CLIENT).setParent(serverSpan).startSpan();
			// try (Scope outgoingScope = tracer.withSpan(restTemplateSpan)) {
			try (Scope outgoingScope = TracingContextUtils.currentContextWith(restTemplateSpan)) {
				// Add some important info to our Span
				restTemplateSpan.addEvent("Calling todomvcui/Home/ToDo via RestTemplate"); // This ends up in "logs"
																							// section in
				// Jaeger
				// Add the attributes defined in the Semantic Conventions
				restTemplateSpan.setAttribute("http.method", "GET");
				restTemplateSpan.setAttribute("http.scheme", "http");
				restTemplateSpan.setAttribute("http.host", "todomvcui:60000");
				restTemplateSpan.setAttribute("http.target", "/Home/ToDo");

				// Execute the header injection that we defined above in the Setter and
				// create HttpEntity to hold the headers (and pass to RestTemplate)
				// Moving to HttpUtils at some point, but not yet (for troubleshooting)
				OpenTelemetry.getPropagators().getTextMapPropagator().inject(Context.current(), propagationHeaders,
						httpHeadersSetter);
				logger.debug("Injecting headers for call from GarageSale item-api/distribute to todomvcui/Home/ToDo");
				logger.debug("**** Here are the headers: " + headers.toString());
				HttpEntity<String> entity = new HttpEntity<String>("parameters", propagationHeaders);

				// Make outgoing call via RestTemplate
				ResponseEntity<String> response = restTemplate.exchange("http://todomvcui:60000/Home/ToDo",
						HttpMethod.GET, entity, String.class);

				// Capture the result that could be passed to our ThymeLeaf view - changed to
				// capture return from HttpUtils
				String responseString = response.getBody();
				// List<Item> listItems = httpUtils.callEndpoint(fullItemApiUrl + "/items");
			} catch (Exception e) {
				restTemplateSpan.addEvent("error");
				restTemplateSpan.addEvent(e.toString());
				restTemplateSpan.setAttribute("error", true);
				logger.error("Error during OT section, here it is!", e);
				return "error";
			} finally {
				restTemplateSpan.end();
			}

			return "It Worked!";
		} catch (Exception e) {
			logger.error("Exception caught attempting to create Span", e);
			return "Didn't work bro!";
		} finally {
			if (serverSpan != null) {
				serverSpan.end();
			}
		}

	}

}
