package com.ktully.appd.otel.ui.Controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import com.ktully.appd.otel.ui.Model.Item;

@Controller
public class HomeController {
	
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
	public String home(@RequestParam(name = "name", required = false, defaultValue = "anonymous") String name, Model model) {
		
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
