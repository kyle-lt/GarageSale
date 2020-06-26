package com.ktully.appd.otel.ui.Controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

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
		
		return "home";
	}

}
