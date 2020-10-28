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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.view.RedirectView;

import com.ktully.appd.otel.ui.Model.Item;

import io.grpc.Context;
import io.opentelemetry.OpenTelemetry;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.Tracer;
import io.opentelemetry.trace.TracingContextUtils;

@Controller
public class HomeController {

	private static final Logger logger = LoggerFactory.getLogger(HomeController.class);

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

	@RequestMapping("/distribute")
	public RedirectView distribute(Model model) {

		// AppD Browser EUM Configs
		model.addAttribute("appdbrumconfigappkey", appdbrumconfigappkey);
		model.addAttribute("appdbrumconfigadrumurlhttp", appdbrumconfigadrumurlhttp);
		model.addAttribute("appdbrumconfigadrumurlhttps", appdbrumconfigadrumurlhttps);
		model.addAttribute("appdbrumconfigbeaconhttp", appdbrumconfigbeaconhttp);
		model.addAttribute("appdbrumconfigbeaconhttps", appdbrumconfigbeaconhttps);
		model.addAttribute("appdbrumconfigpagename", "Home Page");

		// Build full URI for API call
		String fullItemApiUrl = "http://" + itemApiUrl + ":" + itemApiPort;

		// A couple random HTTP Exit Calls via RestTemplate
		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();

		// Start a Span for (and send) RestTemplate
		Span restTemplateSpan = tracer.spanBuilder("/item-api/distribute").setSpanKind(Span.Kind.CLIENT).startSpan();
		// try (Scope outgoingScope = tracer.withSpan(restTemplateSpan)) {
		try (Scope outgoingScope = TracingContextUtils.currentContextWith(restTemplateSpan)) {
			// Add some important info to our Span
			restTemplateSpan.addEvent("Calling item-api/distribute via RestTemplate"); // This ends up in "logs" section in
																			// Jaeger
			// Add the attributes defined in the Semantic Conventions
			restTemplateSpan.setAttribute("http.method", "GET");
			restTemplateSpan.setAttribute("http.scheme", "http");
			restTemplateSpan.setAttribute("http.host", "/:8080");
			restTemplateSpan.setAttribute("http.target", "/distribute");

			// Execute the header injection that we defined above in the Setter and
			// create HttpEntity to hold the headers (and pass to RestTemplate)
			// Moving to HttpUtils at some point, but not yet (for troubleshooting)
			OpenTelemetry.getPropagators().getTextMapPropagator().inject(Context.current(), headers, httpHeadersSetter);
			logger.debug("Injecting headers for call from GarageSale Home /distribute to item-api/distribute");
			logger.debug("**** Here are the headers: " + headers.toString());
			HttpEntity<String> entity = new HttpEntity<String>("parameters", headers);

			// Make outgoing call via RestTemplate
			ResponseEntity<String> response = restTemplate.exchange(fullItemApiUrl + "/distribute", HttpMethod.GET,
					entity, String.class);
			
			// Capture the result that could be passed to our ThymeLeaf view - changed to
			// capture return from HttpUtils
			String responseString = response.getBody();
			// List<Item> listItems = httpUtils.callEndpoint(fullItemApiUrl + "/items");
		} catch (Exception e) {
			restTemplateSpan.addEvent("error");
			restTemplateSpan.addEvent(e.toString());
			restTemplateSpan.setAttribute("error", true);
			logger.error("Error during OT section, here it is!", e);
			return new RedirectView("/error");
		} finally {
			restTemplateSpan.end();
		}

		return new RedirectView("");
	}

	@RequestMapping("/")
	public String index(Model model) {

		// AppD Browser EUM Configs
		model.addAttribute("appdbrumconfigappkey", appdbrumconfigappkey);
		model.addAttribute("appdbrumconfigadrumurlhttp", appdbrumconfigadrumurlhttp);
		model.addAttribute("appdbrumconfigadrumurlhttps", appdbrumconfigadrumurlhttps);
		model.addAttribute("appdbrumconfigbeaconhttp", appdbrumconfigbeaconhttp);
		model.addAttribute("appdbrumconfigbeaconhttps", appdbrumconfigbeaconhttps);
		model.addAttribute("appdbrumconfigpagename", "Home Page");

		// A couple random HTTP Exit Calls via RestTemplate
		RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<String> googleResponse = restTemplate.getForEntity("https://www.google.com", String.class);
		ResponseEntity<String> yahooResponse = restTemplate.getForEntity("https://www.yahoo.com", String.class);

		return "home";
	}

	@RequestMapping("/home")
	public String home(@RequestParam(name = "name", required = false, defaultValue = "anonymous") String name,
			Model model) {

		model.addAttribute("name", name);

		// AppD Browser EUM Configs
		model.addAttribute("appdbrumconfigappkey", appdbrumconfigappkey);
		model.addAttribute("appdbrumconfigadrumurlhttp", appdbrumconfigadrumurlhttp);
		model.addAttribute("appdbrumconfigadrumurlhttps", appdbrumconfigadrumurlhttps);
		model.addAttribute("appdbrumconfigbeaconhttp", appdbrumconfigbeaconhttp);
		model.addAttribute("appdbrumconfigbeaconhttps", appdbrumconfigbeaconhttps);
		model.addAttribute("appdbrumconfigpagename", "Home Page");

		return "home";
	}

}
