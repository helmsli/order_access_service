package com.company.orderAccess.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
@Controller
@RequestMapping("/home")
public class UserController {
	@RequestMapping(method=RequestMethod.GET)
	public String list(Model model) {
		return "user/home";
	}
	@RequestMapping(value = "{name}" ,method=RequestMethod.GET)
	public String list(@PathVariable String name) {
		return "user/" + name;
	}
	
}
