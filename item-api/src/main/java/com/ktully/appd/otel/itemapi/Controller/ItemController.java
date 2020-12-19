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

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.TextMapPropagator;


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
		// 0.10.0 - didn't need this implementation for 0.8.0
		@Override
		public Iterable<String> keys(Map<String, String> carrier) {
			return carrier.keySet();
		}
	};

	/*
	 * Configuration for Context Propagation to be done via HttpHeaders injection
	 */
	private static final TextMapPropagator.Setter<Map<String, String>> httpHeadersSetter = new TextMapPropagator.Setter<Map<String, String>>() {
		@Override
		public void set(Map<String, String> carrier, String key, String value) {
			logger.debug("RestTemplate - Adding Header with Key = " + key);
			logger.debug("RestTemplate - Adding Header with Value = " + value);
			carrier.put(key, value);
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
			// 0.10.0
			//extractedContext = OpenTelemetry.getGlobalPropagators()
		      //        .getTextMapPropagator()
		        //      .extract(Context.current(), headers, getter);
			// 0.13.1
			extractedContext = GlobalOpenTelemetry.getPropagators().getTextMapPropagator()
					.extract(Context.current(), headers, getter);
			
			logger.debug(extractedContext.toString());
		} catch (Exception e) {
			logger.error("Exception caught while extracting Context Propagators", e);
		}
		
		// 0.10.0
		Span serverSpan = tracer.spanBuilder("GET /item-api/items").setParent(extractedContext).setSpanKind(Span.Kind.SERVER).startSpan();
		try (Scope scope = serverSpan.makeCurrent()) {
			logger.debug("Trying to build Span and then make DB call.");
			// Add the attributes defined in the Semantic Conventions
			serverSpan.setAttribute("http.method", "GET");
			serverSpan.setAttribute("http.scheme", "http");
			serverSpan.setAttribute("http.host", "item-api:8081");
			serverSpan.setAttribute("http.target", "/items");

			List<ItemModel> items = null;
			// 0.10.0
			// TODO
			items = itemService.getAllItems();
			// 0.8.0
			//try (Scope itemServiceScope = tracer.withSpan(serverSpan)) {
			//	items = itemService.getAllItems();
			//}
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
			// 0.10.0
			//extractedContext = OpenTelemetry.getGlobalPropagators()
		      //        .getTextMapPropagator()
		        //      .extract(Context.current(), headers, getter);
			// 0.13.1
			extractedContext = GlobalOpenTelemetry.getPropagators().getTextMapPropagator()
					.extract(Context.current(), headers, getter);
		} catch (Exception e) {
			logger.error("Exception caught while extracting Context Propagators", e);
		}

		//try (Scope scope = ContextUtils.withScopedContext(extractedContext)) {
		// 0.10.0
		Span serverSpan = tracer.spanBuilder("GET /item-api/item/{id}").setParent(extractedContext).setSpanKind(Span.Kind.SERVER).startSpan();
		try (Scope scope = serverSpan.makeCurrent()) {
			logger.debug("Trying to build Span and then make DB call.");
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
			// 0.10.0
			//extractedContext = OpenTelemetry.getGlobalPropagators()
		      //        .getTextMapPropagator()
		        //      .extract(Context.current(), headers, getter);
			// 0.13.1
			extractedContext = GlobalOpenTelemetry.getPropagators().getTextMapPropagator()
					.extract(Context.current(), headers, getter);
		} catch (Exception e) {
			logger.error("Exception caught while extracting Context Propagators", e);
		}

		// 0.10.0
		Span serverSpan = tracer.spanBuilder("POST /item-api/item").setParent(extractedContext).setSpanKind(Span.Kind.SERVER).startSpan();
		try (Scope scope = serverSpan.makeCurrent()) {
			// Automatically use the extracted SpanContext as parent.
			logger.debug("Trying to build Span and then make DB call.");
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
			// 0.10.0
			//extractedContext = OpenTelemetry.getGlobalPropagators()
		      //        .getTextMapPropagator()
		        //      .extract(Context.current(), headers, getter);
			// 0.13.1
			extractedContext = GlobalOpenTelemetry.getPropagators().getTextMapPropagator()
					.extract(Context.current(), headers, getter);
			
			logger.debug(extractedContext.toString());
		} catch (Exception e) {
			logger.error("Exception caught while extracting Context Propagators", e);
		}

		// 0.10.0
		Span serverSpan = tracer.spanBuilder("GET /distribute").setParent(extractedContext).setSpanKind(Span.Kind.SERVER).startSpan();
		try (Scope scope = serverSpan.makeCurrent()) {
			logger.debug("Trying to build Span and then make RestTemplate call to .NET Core App.");
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
			// 0.8.0
			//Span restTemplateSpan = tracer.spanBuilder("GET /todomvcui/Home/ToDo").setSpanKind(Span.Kind.CLIENT).setParent(serverSpan).startSpan();
			// try (Scope outgoingScope = tracer.withSpan(restTemplateSpan)) {
			//try (Scope outgoingScope = TracingContextUtils.currentContextWith(restTemplateSpan)) {
			
			// 0.10.0
			Span restTemplateSpan = tracer.spanBuilder("GET /todomvcui/Home/ToDo").setSpanKind(Span.Kind.CLIENT).startSpan();
			try (Scope outgoingScope = restTemplateSpan.makeCurrent()) {
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
				// 0.8.0
				//OpenTelemetry.getPropagators().getTextMapPropagator().inject(Context.current(), propagationHeaders,
				//		httpHeadersSetter);
				// 0.10.0
				//OpenTelemetry.getGlobalPropagators().getTextMapPropagator().inject(Context.current(), headers, httpHeadersSetter);
				// 0.13.1
				GlobalOpenTelemetry.getPropagators().getTextMapPropagator().inject(Context.current(), headers, httpHeadersSetter);
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
