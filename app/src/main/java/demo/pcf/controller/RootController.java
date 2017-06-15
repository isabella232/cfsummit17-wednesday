package demo.pcf.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import demo.pcf.HitCounter;

@Controller
public class RootController {

	@Autowired
	HitCounter hitCounter;
		
	@GetMapping("/")
	public ModelAndView index() {
		this.hitCounter.hitAnyPage();
		this.hitCounter.hitRootPage();
		
		return new ModelAndView("index");
	}
	
}
