package com.ktully.appd.otel.ui.Controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.Builder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.ktully.appd.otel.ui.OtelTracerConfig;
import com.ktully.appd.otel.ui.Model.Item;

import reactor.core.publisher.Flux;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.TextMapSetter;

@Controller
public class ItemController {

	private static final Logger logger = LoggerFactory.getLogger(ItemController.class);
	
	private static String UPLOAD_FOLDER = "/app/images/";

	@Value("${item.api.url}")
	private String itemApiUrl;
	@Value("${item.api.port}")
	private String itemApiPort;
	
	// AppD Browser EUM Configs
	@Value("${appdbrumconfigappkey}")
	private String appdbrumconfigappkey;
	@Value("${appdbrumconfigadrumurlhttp}")
	private String appdbrumconfigadrumurlhttp;
	@Value("${appdbrumconfigadrumurlhttps}")
	private String appdbrumconfigadrumurlhttps;
	@Value("${appdbrumconfigbeaconhttp}")
	private String appdbrumconfigbeaconhttp;
	@Value("${appdbrumconfigbeaconhttps}")
	private String appdbrumconfigbeaconhttps;
	
	Item item = null;

	//@Autowired
	//Tracer tracer;
	
	//@Autowired
	//static OpenTelemetry openTelemetry;
	private static final OpenTelemetry openTelemetry = OtelTracerConfig.OpenTelemetryConfig();
	
	private static final Tracer tracer =
		      openTelemetry.getTracer("com.ktully.appd.otel.ui");
	
	//@Autowired
	//private HttpUtils httpUtils;
	
	/*
	 * Configuration for Context Propagation to be done via HttpHeaders injection
	 */
	private static final TextMapSetter<HttpHeaders> httpHeadersSetter = new TextMapSetter<HttpHeaders>() {
		@Override
		public void set(HttpHeaders carrier, String key, String value) {
			logger.debug("RestTemplate - Adding Header with Key = " + key);
			logger.debug("RestTemplate - Adding Header with Value = " + value);
			carrier.set(key, value);
		}
	};
	
	/*
	 * Configuration for Context Propagation to be done via injection into WebClient
	 * Builder headers
	 */
	private static final TextMapSetter<Builder> webClientSetter = new TextMapSetter<Builder>() {
		@Override
		public void set(Builder carrier, String key, String value) {
			logger.debug("WebClient - Adding Header with Key = " + key);
			logger.debug("WebClient - Adding Header with Value = " + value);
			carrier.defaultHeader(key, value);
		}
	};

	@RequestMapping("/items")
	public String items(Model model) {

		// Start a Parent Span for "/items"
		Span parentSpan = tracer.spanBuilder("GET /items").setSpanKind(SpanKind.CLIENT).startSpan();
		//try (Scope scope = tracer.withSpan(parentSpan)) {
		// 0.8.0
		//try (Scope scope = TracingContextUtils.currentContextWith(parentSpan)) {
		// 0.10.0
		try (Scope scope = parentSpan.makeCurrent()) {

			// Add the attributes defined in the Semantic Conventions
			parentSpan.setAttribute("http.method", "GET");
			parentSpan.setAttribute("http.scheme", "http");
			parentSpan.setAttribute("http.host", ":8080");
			parentSpan.setAttribute("http.target", "/items");
			// Build full URI for API call
			String fullItemApiUrl = "http://" + itemApiUrl + ":" + itemApiPort;

			/*
			 * *****************************************************************************
			 * *** START *** RestTemplate for nostalgia ***
			 */
			RestTemplate restTemplate = new RestTemplate();
			HttpHeaders headers = new HttpHeaders();

			// Start a Span for (and send) RestTemplate
			Span restTemplateSpan = tracer.spanBuilder("GET /item-api/items:RestTemplate").setSpanKind(SpanKind.CLIENT)
					.startSpan();
			//try (Scope outgoingScope = tracer.withSpan(restTemplateSpan)) {
			// 0.8.0
			//try (Scope outgoingScope = TracingContextUtils.currentContextWith(restTemplateSpan)) {
			// 0.10.0
			try (Scope outgoingScope = restTemplateSpan.makeCurrent()) {
				// Add some important info to our Span
				restTemplateSpan.addEvent("Calling item-api via RestTemplate"); // This ends up in "logs" section in
																				// Jaeger
				// Add the attributes defined in the Semantic Conventions
				restTemplateSpan.setAttribute("http.method", "GET");
				restTemplateSpan.setAttribute("http.scheme", "http");
				restTemplateSpan.setAttribute("http.host", "item-api:8081");
				restTemplateSpan.setAttribute("http.target", "/items");

				// Execute the header injection that we defined above in the Setter and
				// create HttpEntity to hold the headers (and pass to RestTemplate)
				// Moving to HttpUtils at some point, but not yet (for troubleshooting)
				// 0.8.0
				//OpenTelemetry.getPropagators().getTextMapPropagator().inject(Context.current(), headers,
				//		httpHeadersSetter);
				// 0.10.0
				//OpenTelemetry.getGlobalPropagators().getTextMapPropagator().inject(Context.current(), headers, httpHeadersSetter);
				// 0.13.1
				//GlobalOpenTelemetry.getPropagators().getTextMapPropagator().inject(Context.current(), headers, httpHeadersSetter);
				// 0.14.1
				openTelemetry.getPropagators().getTextMapPropagator().inject(Context.current(), headers, httpHeadersSetter);
				
				
				logger.debug("**** Here are the headers: " + headers.toString());
				HttpEntity<String> entity = new HttpEntity<String>("parameters", headers);

				// Make outgoing call via RestTemplate - moved to HttpUtils
				ResponseEntity<List<Item>> itemResponse = restTemplate.exchange(fullItemApiUrl + "/items",
						HttpMethod.GET, entity, new ParameterizedTypeReference<List<Item>>() {
						});

				// Capture the result that could be passed to our ThymeLeaf view - changed to capture return from HttpUtils
				List<Item> listItems = itemResponse.getBody();
				//List<Item> listItems = httpUtils.callEndpoint(fullItemApiUrl + "/items");
			} catch (Exception e) {
				restTemplateSpan.addEvent("error");
				restTemplateSpan.addEvent(e.toString());
				restTemplateSpan.setAttribute("error", true);
				logger.error("Error during OT section, here it is!", e);
				return "error";
			} finally {
				restTemplateSpan.end();
			}
			/*
			 * *****************************************************************************
			 * *** END *** RestTemplate for nostalgia ***
			 */


			/*
			 * *****************************************************************************
			 * *** START **** WebClient for future-proofing ***
			 */

			// Create a builder to be injected with Context Propagation headers
			Builder webClientBuilder = WebClient.builder();

			// Start a Span for (and send) WebClient
			Span webClientSpan = tracer.spanBuilder("GET /item-api:WebClient").setSpanKind(SpanKind.CLIENT).startSpan();
			// 0.8.0
			//try (Scope outgoingScope = tracer.withSpan(webClientSpan)) {
			// 0.10.0
			try (Scope outgoingScope = webClientSpan.makeCurrent()) {
				// Add some important info to our Span
				webClientSpan.addEvent("Calling item-api via WebClient"); // This ends up in "logs" section in Jaeger
				// Add the attributes defined in the Semantic Conventions
				webClientSpan.setAttribute("http.method", "GET");
				webClientSpan.setAttribute("http.scheme", "http");
				webClientSpan.setAttribute("http.host", "item-api:8081");
				webClientSpan.setAttribute("http.target", "/items");

				// Execute the header injection that we defined above in the Setter and
				// create HttpEntity to hold the headers (and pass to RestTemplate)
				// 0.8.0
				//OpenTelemetry.getPropagators().getTextMapPropagator().inject(Context.current(), webClientBuilder,
				//		webClientSetter);
				// 0.10.0
				//OpenTelemetry.getGlobalPropagators().getTextMapPropagator().inject(Context.current(), webClientBuilder, webClientSetter);
				// 0.13.1
				//GlobalOpenTelemetry.getPropagators().getTextMapPropagator().inject(Context.current(), webClientBuilder, webClientSetter);
				// 0.14.1
				openTelemetry.getPropagators().getTextMapPropagator().inject(Context.current(), webClientBuilder, webClientSetter);
				
				// Make outgoing call via RestTemplate
				WebClient webClient = webClientBuilder.baseUrl(fullItemApiUrl)
						.defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE).build();

				Flux<Item> fluxItems = webClient.get().uri("/items").retrieve().bodyToFlux(Item.class);

				// Capture the result that could be passed to our ThymeLeaf view - huge blocking call!
				List<Item> fluxListItems = fluxItems.collectList().block();

				// Add the resulting list to our ThymeLeaf View
				model.addAttribute("listItems", fluxListItems);
				
				// AppD Browser EUM Configs
				model.addAttribute("appdbrumconfigappkey", appdbrumconfigappkey);
				model.addAttribute("appdbrumconfigadrumurlhttp", appdbrumconfigadrumurlhttp);
				model.addAttribute("appdbrumconfigadrumurlhttps", appdbrumconfigadrumurlhttps);
				model.addAttribute("appdbrumconfigbeaconhttp", appdbrumconfigbeaconhttp);
				model.addAttribute("appdbrumconfigbeaconhttps", appdbrumconfigbeaconhttps);
				model.addAttribute("appdbrumconfigpagename", "Items");

				// Return the ThymeLeaf View
				return "items";
			} catch (Exception e) {
				webClientSpan.addEvent("error");
				webClientSpan.addEvent(e.toString());
				webClientSpan.setAttribute("error", true);
				logger.error("Error during OT section, here it is!", e);
				return "error";
			} finally {
				webClientSpan.end();
			}
			/*
			 * *****************************************************************************
			 * *** END **** WebClient for future-proofing ***
			 */
		} finally {
			parentSpan.end();
		}

	}

	@RequestMapping("item/{id}")
	public String item(@PathVariable("id") String id, Model model) {

		// Start a Parent Span for "/items/{id}"
		Span parentSpan = tracer.spanBuilder("GET /item/{id}").setSpanKind(SpanKind.CLIENT).startSpan();
		// 0.8.0
		//try (Scope scope = tracer.withSpan(parentSpan)) {
		// 0.10.0
		try (Scope scope = parentSpan.makeCurrent()) {

			// Add the attributes defined in the Semantic Conventions
			parentSpan.setAttribute("http.method", "GET");
			parentSpan.setAttribute("http.scheme", "http");
			parentSpan.setAttribute("http.host", ":8080");
			parentSpan.setAttribute("http.target", "/item/{id}");
			// Build full URI for API call
			String fullItemApiUrl = "http://" + itemApiUrl + ":" + itemApiPort;

			/*
			 * *****************************************************************************
			 * *** START *** RestTemplate for nostalgia ***
			 */
			RestTemplate restTemplate = new RestTemplate();
			HttpHeaders headers = new HttpHeaders();

			// Start a Span for (and send) RestTemplate
			Span restTemplateSpan = tracer.spanBuilder("GET /item-api:RestTemplate").setSpanKind(SpanKind.CLIENT)
					.startSpan();
			// 0.8.0
			//try (Scope outgoingScope = tracer.withSpan(restTemplateSpan)) {
			// 0.10.0
			try (Scope outgoingScope = restTemplateSpan.makeCurrent()) {
				// Add some important info to our Span
				restTemplateSpan.addEvent("Calling item-api via RestTemplate"); // This ends up in "logs" section in
																				// Jaeger
				// Add the attributes defined in the Semantic Conventions
				restTemplateSpan.setAttribute("http.method", "GET");
				restTemplateSpan.setAttribute("http.scheme", "http");
				restTemplateSpan.setAttribute("http.host", "item-api:8081");
				restTemplateSpan.setAttribute("http.target", "/item/{id}");

				// Execute the header injection that we defined above in the Setter and
				// create HttpEntity to hold the headers (and pass to RestTemplate)
				// 0.8.0
				//OpenTelemetry.getPropagators().getTextMapPropagator().inject(Context.current(), headers,
				//		httpHeadersSetter);
				// 0.10.0
				//OpenTelemetry.getGlobalPropagators().getTextMapPropagator().inject(Context.current(), headers, httpHeadersSetter);
				// 0.13.1
				//GlobalOpenTelemetry.getPropagators().getTextMapPropagator().inject(Context.current(), headers, httpHeadersSetter);
				// 0.14.1
				openTelemetry.getPropagators().getTextMapPropagator().inject(Context.current(), headers, httpHeadersSetter);
				
				logger.debug("**** Here are the headers: " + headers.toString());
				HttpEntity<String> entity = new HttpEntity<String>("parameters", headers);

				// Make outgoing call via RestTemplate
				ResponseEntity<Item> itemResponse = restTemplate.exchange(fullItemApiUrl + "/item/" + id,
						HttpMethod.GET, entity, new ParameterizedTypeReference<Item>() {
						});

				// Capture the result that could be passed to our ThymeLeaf view
				Item itemNotUsed = itemResponse.getBody();
				
				logger.debug("itemNotUsed.id = " + itemNotUsed.getId());
				logger.debug("itemNotUsed.category = " + itemNotUsed.getCategory());
				logger.debug("itemNotUsed.name = " + itemNotUsed.getName());
				logger.debug("itemNotUsed.price = " + itemNotUsed.getPrice());
				
				//model.addAttribute("item", itemNotUsed);
				
				//return "item";
			} catch (Exception e) {
				restTemplateSpan.addEvent("error");
				restTemplateSpan.addEvent(e.toString());
				restTemplateSpan.setAttribute("error", true);
				logger.error("Error during OT section, here it is!", e);
				return "error";
			} finally {
				restTemplateSpan.end();
			}
			/*
			 * *****************************************************************************
			 * *** END *** RestTemplate for nostalgia ***
			 */

			/*
			 * *****************************************************************************
			 * *** START **** WebClient for future-proofing ***
			 */

			// Create a builder to be injected with Context Propagation headers
			Builder webClientBuilder = WebClient.builder();

			// Start a Span for (and send) WebClient
			Span webClientSpan = tracer.spanBuilder("GET /item-api:WebClient").setSpanKind(SpanKind.CLIENT).startSpan();
			// 0.8.0
			//try (Scope outgoingScope = tracer.withSpan(webClientSpan)) {
			// 0.10.0
			try (Scope outgoingScope = webClientSpan.makeCurrent()) {
				// Add some important info to our Span
				webClientSpan.addEvent("Calling item-api via WebClient"); // This ends up in "logs" section in Jaeger
				// Add the attributes defined in the Semantic Conventions
				webClientSpan.setAttribute("http.method", "GET");
				webClientSpan.setAttribute("http.scheme", "http");
				webClientSpan.setAttribute("http.host", "item-api:8081");
				webClientSpan.setAttribute("http.target", "/item/{id}");

				// Execute the header injection that we defined above in the Setter and
				// create HttpEntity to hold the headers (and pass to RestTemplate)
				// 0.8.0
				//OpenTelemetry.getPropagators().getTextMapPropagator().inject(Context.current(), webClientBuilder,
				//			webClientSetter);
				// 0.10.0
				//OpenTelemetry.getGlobalPropagators().getTextMapPropagator().inject(Context.current(), webClientBuilder, webClientSetter);
				// 0.13.1
				//GlobalOpenTelemetry.getPropagators().getTextMapPropagator().inject(Context.current(), webClientBuilder, webClientSetter);
				// 0.14.1
				openTelemetry.getPropagators().getTextMapPropagator().inject(Context.current(), webClientBuilder, webClientSetter);
				
				// Make outgoing call via RestTemplate
				WebClient webClient = webClientBuilder.baseUrl(fullItemApiUrl)
						.defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE).build();

				Flux<Item> fluxItems = webClient.get().uri("/item/" + id).retrieve().bodyToFlux(Item.class);
				 
				Item item = fluxItems.blockFirst();
				
				logger.debug("item.id = " + item.getId());
				logger.debug("item.category = " + item.getCategory());
				logger.debug("item.name = " + item.getName());
				logger.debug("item.price = " + item.getPrice());
				
				// Add the resulting item to our ThymeLeaf View
				model.addAttribute("item", item);
				
				// AppD Browser EUM Configs
				model.addAttribute("appdbrumconfigappkey", appdbrumconfigappkey);
				model.addAttribute("appdbrumconfigadrumurlhttp", appdbrumconfigadrumurlhttp);
				model.addAttribute("appdbrumconfigadrumurlhttps", appdbrumconfigadrumurlhttps);
				model.addAttribute("appdbrumconfigbeaconhttp", appdbrumconfigbeaconhttp);
				model.addAttribute("appdbrumconfigbeaconhttps", appdbrumconfigbeaconhttps);
				model.addAttribute("appdbrumconfigpagename", "Item");

				// Return the ThymeLeaf View
				return "item";
			} catch (Exception e) {
				webClientSpan.addEvent("error");
				webClientSpan.addEvent(e.toString());
				webClientSpan.setAttribute("error", true);
				logger.error("Error during OT section, here it is!", e);
				return "error";
			} finally {
				webClientSpan.end();
			}
			/*
			 * *****************************************************************************
			 * *** END **** WebClient for future-proofing ***
			 */
		} finally {
			parentSpan.end();
		}

	}
	
	@RequestMapping("item/{id}/{image}")
	public @ResponseBody byte[] getImage(@PathVariable("id") String id, @PathVariable("image") String image) throws IOException {
		
		Path path = Paths.get(UPLOAD_FOLDER + id + "/" + image);
		byte[] imageBytes = Files.readAllBytes(path);
		
		return imageBytes;
		
	}

}
