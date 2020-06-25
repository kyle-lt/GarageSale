package com.ktully.appd.otel.ui.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.ktully.appd.otel.ui.Model.Item;

@Controller
public class PostController {
	
	@RequestMapping("/post")
	public String post(Model model) {
		model.addAttribute("item", new Item());
		return "post";
	}
	
	@PostMapping("/post")
	public String postItem(@ModelAttribute Item item) {
		return "index";
	}

}
