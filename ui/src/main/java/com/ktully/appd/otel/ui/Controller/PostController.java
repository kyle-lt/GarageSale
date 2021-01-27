package com.ktully.appd.otel.ui.Controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
//import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
//import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
//import org.springframework.core.io.ClassPathResource;

import com.fasterxml.jackson.databind.ObjectMapper;
//import com.google.gson.Gson;
import com.ktully.appd.otel.ui.OtelTracerConfig;
import com.ktully.appd.otel.ui.Model.Item;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;

//import io.grpc.Context;

// 0.8.0
//import io.opentelemetry.OpenTelemetry;
//import io.opentelemetry.context.Scope;
//import io.opentelemetry.context.propagation.TextMapPropagator;
//import io.opentelemetry.trace.Span;
//import io.opentelemetry.trace.Tracer;

//0.10.0
//import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
//import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
//import io.opentelemetry.api.trace.propagation.HttpTraceContext;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
//import io.opentelemetry.context.propagation.DefaultContextPropagators;
import io.opentelemetry.context.propagation.TextMapPropagator;
//import io.opentelemetry.exporter.logging.LoggingSpanExporter;
//import io.opentelemetry.sdk.OpenTelemetrySdk;
//import io.opentelemetry.sdk.trace.SpanProcessor;
//import io.opentelemetry.sdk.trace.TracerSdkManagement;
//import io.opentelemetry.sdk.trace.config.TraceConfig;
//import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;

@Controller
public class PostController {

	private static final Logger logger = LoggerFactory.getLogger(PostController.class);
	
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

	//@Autowired
	//Tracer tracer;
	
	//@Autowired
	//static OpenTelemetry openTelemetry;
	private static final OpenTelemetry openTelemetry = OtelTracerConfig.OpenTelemetryConfig();
	
	private static final Tracer tracer =
		      openTelemetry.getTracer("com.ktully.appd.otel.ui");

	@RequestMapping("/post")
	public String post(Model model) {
		
		model.addAttribute("item", new Item());
		
		// AppD Browser EUM Configs
		model.addAttribute("appdbrumconfigappkey", appdbrumconfigappkey);
		model.addAttribute("appdbrumconfigadrumurlhttp", appdbrumconfigadrumurlhttp);
		model.addAttribute("appdbrumconfigadrumurlhttps", appdbrumconfigadrumurlhttps);
		model.addAttribute("appdbrumconfigbeaconhttp", appdbrumconfigbeaconhttp);
		model.addAttribute("appdbrumconfigbeaconhttps", appdbrumconfigbeaconhttps);
		model.addAttribute("appdbrumconfigpagename", "Post");
		
		return "post";
	}

	@PostMapping("/post")
	public String postItem(@ModelAttribute Item item, Model model, RedirectAttributes redirectAttributes,
			@RequestParam("file") MultipartFile file) {
		item.setId(-1); // Send something except null
		item.setImage(file.getOriginalFilename()); // grab the original file name
		
		// Start with RestTemplate, the add webClient, then add Otel

		// Start a Parent Span for "/items"
		Span parentSpan = tracer.spanBuilder("POST /post").setSpanKind(Span.Kind.CLIENT).startSpan();
		// 0.8.0
		//try (Scope scope = tracer.withSpan(parentSpan)) {
		// 0.10.0
		try (Scope scope = parentSpan.makeCurrent()) {

			// Add the attributes defined in the Semantic Conventions
			parentSpan.setAttribute("http.method", "POST");
			parentSpan.setAttribute("http.scheme", "http");
			parentSpan.setAttribute("http.host", ":8080");
			parentSpan.setAttribute("http.target", "/post");
			// Build full URI for API call
			String fullItemApiUrl = "http://" + itemApiUrl + ":" + itemApiPort;

			/*
			 * Configuration for Context Propagation to be done via HttpHeaders injection
			 */
			TextMapPropagator.Setter<HttpHeaders> httpHeadersSetter = new TextMapPropagator.Setter<HttpHeaders>() {
				@Override
				public void set(HttpHeaders carrier, String key, String value) {
					logger.debug("RestTemplate - Adding Header with Key = " + key);
					logger.debug("RestTemplate - Adding Header with Value = " + value);
					carrier.add(key, value);
				}
			};

			/*
			 * *****************************************************************************
			 * *** START *** RestTemplate ***
			 */
			RestTemplate restTemplate = new RestTemplate();
			HttpHeaders headers = new HttpHeaders();

			// Start a Span for (and send) RestTemplate
			Span restTemplateSpan = tracer.spanBuilder("POST /item-api:RestTemplate").setSpanKind(Span.Kind.CLIENT)
					.startSpan();
			// 0.8.0
			//try (Scope outgoingScope = tracer.withSpan(restTemplateSpan)) {
			// 0.10.0
			try (Scope outgoingScope = restTemplateSpan.makeCurrent()) {
				
				// Add the attributes defined in the Semantic Conventions
				restTemplateSpan.setAttribute("http.method", "POST");
				restTemplateSpan.setAttribute("http.scheme", "http");
				restTemplateSpan.setAttribute("http.host", "item-api:8081");
				restTemplateSpan.setAttribute("http.target", "/item");				
				
				// Add some important info to our Span
				restTemplateSpan.addEvent("Calling item-api via RestTemplate"); // This ends up in "logs" section in
																				// Jaeger
				restTemplateSpan.setAttribute("item-RT-Key", "item-RT-Value");

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
				
				// Add Content-Type Header
				headers.add("Content-Type", "application/json");
				
				logger.debug("**** Here are the headers: " + headers.toString());

				// Build the JSON payload from the Item created from Form data
				ObjectMapper mapper = new ObjectMapper();
				String itemJsonString = mapper.writeValueAsString(item);
				logger.debug("item as String: " + itemJsonString);
				
				// Build the HttpEntity with our JSON payload and headers
				HttpEntity<String> entity = new HttpEntity<String>(itemJsonString, headers);

				// Send the HTTP POST and capture the result into a ResponseEntity<String>
				ResponseEntity<String> itemPostResponse = restTemplate.exchange(fullItemApiUrl + "/item",
						HttpMethod.POST, entity, new ParameterizedTypeReference<String>() {
						});
				
				logger.debug("itemPostResponse.getBody() = " + itemPostResponse.getBody());
				
				//model.addAttribute("itemId", itemPostResponse.getBody());
				
				// Send resulting itemId to the postResult redirect page
				redirectAttributes.addAttribute("itemId", itemPostResponse.getBody());
				
				// Add code to persist image upload to local disk
				
		        try {
		            // Get the file and save it somewhere
		            byte[] bytes = file.getBytes();
		            Path path = Paths.get(UPLOAD_FOLDER + itemPostResponse.getBody() + "/" + file.getOriginalFilename());
		            logger.debug("@@@@@@@Writing file to (path.toString()): " + path.toString());
		            Path parentDir = path.getParent();
		            Files.createDirectories(parentDir);
		            Files.write(path, bytes);

		        } catch (IOException e) {
		            e.printStackTrace();
		        }

				// Capture the result that could be passed to our ThymeLeaf view
				// List<Item> listItems = itemResponse.getBody();
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
			 * *** END *** RestTemplate ***
			 */
		}
		finally {
			parentSpan.end();	
		}

		// TODO - Add support for WebClient

		return "redirect:/postResult";
	}
	
	@RequestMapping("/postResult")
	public String postResult(Model model, @RequestParam(name = "itemId") String itemId) {
		
		model.addAttribute("itemId", itemId);
		
		// AppD Browser EUM Configs
		model.addAttribute("appdbrumconfigappkey", appdbrumconfigappkey);
		model.addAttribute("appdbrumconfigadrumurlhttp", appdbrumconfigadrumurlhttp);
		model.addAttribute("appdbrumconfigadrumurlhttps", appdbrumconfigadrumurlhttps);
		model.addAttribute("appdbrumconfigbeaconhttp", appdbrumconfigbeaconhttp);
		model.addAttribute("appdbrumconfigbeaconhttps", appdbrumconfigbeaconhttps);
		model.addAttribute("appdbrumconfigpagename", "Post Result");
		
		return "postResult";
	}

}
