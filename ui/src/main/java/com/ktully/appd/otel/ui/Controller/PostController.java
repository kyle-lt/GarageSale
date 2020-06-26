package com.ktully.appd.otel.ui.Controller;

import java.util.List;

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
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

import com.ktully.appd.otel.ui.Model.Item;

import io.grpc.Context;
import io.opentelemetry.OpenTelemetry;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.HttpTextFormat;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.Tracer;

@Controller
public class PostController {

	private static final Logger logger = LoggerFactory.getLogger(PostController.class);

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

	@Autowired
	Tracer tracer;

	@RequestMapping("/post")
	public String post(Model model) {
		
		model.addAttribute("item", new Item());
		
		// AppD Browser EUM Configs
		model.addAttribute("appdbrumconfigappkey", appdbrumconfigappkey);
		model.addAttribute("appdbrumconfigadrumurlhttp", appdbrumconfigadrumurlhttp);
		model.addAttribute("appdbrumconfigadrumurlhttps", appdbrumconfigadrumurlhttps);
		model.addAttribute("appdbrumconfigbeaconhttp", appdbrumconfigbeaconhttp);
		model.addAttribute("appdbrumconfigbeaconhttps", appdbrumconfigbeaconhttps);
		
		return "post";
	}

	@PostMapping("/post")
	public String postItem(@ModelAttribute Item item, Model model) {
		item.setId(-1); // Send something except null
		// Start with RestTemplate, the add webClient, then add Otel

		// Start a Parent Span for "/items"
		Span parentSpan = tracer.spanBuilder("/post").setSpanKind(Span.Kind.CLIENT).startSpan();
		try (Scope scope = tracer.withSpan(parentSpan)) {

			// Build full URI for API call
			String fullItemApiUrl = "http://" + itemApiUrl + ":" + itemApiPort;

			/*
			 * Configuration for Context Propagation to be done via HttpHeaders injection
			 */
			HttpTextFormat.Setter<HttpHeaders> httpHeadersSetter = new HttpTextFormat.Setter<HttpHeaders>() {
				@Override
				public void set(HttpHeaders carrier, String key, String value) {
					logger.debug("RestTemplate - Adding Header with Key = " + key);
					logger.debug("RestTemplate - Adding Header with Value = " + value);
					carrier.add(key, value);
				}
			};

			/*
			 * *****************************************************************************
			 * *** START *** RestTemplate for nostalgia ***
			 */
			RestTemplate restTemplate = new RestTemplate();
			HttpHeaders headers = new HttpHeaders();

			// Start a Span for (and send) RestTemplate
			Span restTemplateSpan = tracer.spanBuilder("/item-api:RestTemplate").setSpanKind(Span.Kind.CLIENT)
					.startSpan();
			try (Scope outgoingScope = tracer.withSpan(restTemplateSpan)) {
				// Add some important info to our Span
				restTemplateSpan.addEvent("Calling item-api via RestTemplate"); // This ends up in "logs" section in
																				// Jaeger
				restTemplateSpan.setAttribute("item-RT-Key", "item-RT-Value");

				// Execute the header injection that we defined above in the Setter and
				// create HttpEntity to hold the headers (and pass to RestTemplate)
				OpenTelemetry.getPropagators().getHttpTextFormat().inject(Context.current(), headers,
						httpHeadersSetter);
				
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
				
				model.addAttribute("itemId", itemPostResponse.getBody());
				
				// AppD Browser EUM Configs
				model.addAttribute("appdbrumconfigappkey", appdbrumconfigappkey);
				model.addAttribute("appdbrumconfigadrumurlhttp", appdbrumconfigadrumurlhttp);
				model.addAttribute("appdbrumconfigadrumurlhttps", appdbrumconfigadrumurlhttps);
				model.addAttribute("appdbrumconfigbeaconhttp", appdbrumconfigbeaconhttp);
				model.addAttribute("appdbrumconfigbeaconhttps", appdbrumconfigbeaconhttps);

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
			 * *** END *** RestTemplate for nostalgia ***
			 */
		}
		finally {
			parentSpan.end();	
		}

		// TODO - Add support for WebClient

		return "postResult";
	}

}
