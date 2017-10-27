package com.company.orderAccess.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/video")
public class VideoController {
	@RequestMapping(value = "{name}" ,method=RequestMethod.GET)
	public String list(@PathVariable String name) {
		return "video/" + name;
	}
}
