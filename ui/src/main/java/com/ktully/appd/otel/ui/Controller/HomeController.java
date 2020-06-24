package com.ktully.appd.otel.ui.Controller;

import java.security.Principal;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HomeController {
	
	@RequestMapping("/")
    String index(Principal principal) {
        return principal != null ? "homeSignedIn" : "homeNotSignedIn";
    }
	
	@RequestMapping("/home")
	public String home(@RequestParam(name = "name", required = false, defaultValue = "anonymous") String name, Model model) {
		
		model.addAttribute("name", name);
		
		return "home";
	}

}
