package com.company.orderAccess.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class RootController {
	@RequestMapping("/")
	public String onRootAccess() {
		return "redirect:/home";
	}
}
