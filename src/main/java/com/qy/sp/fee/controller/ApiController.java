package com.qy.sp.fee.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(value = "/api")
public class ApiController {

	@RequestMapping(value = "/apiList")
	public String apiList() {

		return "/apiList";
	}

	@RequestMapping(value = "/getSms")
	public String getSms() {

		return "/getSms";
	}
	@RequestMapping(value = "/appendix")
	public String appendix() {
		
		return "/appendix";
	}
	
}
