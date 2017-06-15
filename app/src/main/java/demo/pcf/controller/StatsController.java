package demo.pcf.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import demo.pcf.HitCounter;
import demo.pcf.PersonRepository;

@Controller
public class StatsController {

	@Autowired
	HitCounter hitCounter;
	@Autowired
	PersonRepository personRepository;
		
	@GetMapping("stats")
	public ModelAndView stats() {
		this.hitCounter.hitAnyPage();

		ModelAndView modelAndView = new ModelAndView("stats/index");
		
		modelAndView.addObject("countAny", this.hitCounter.getCountAny());
		modelAndView.addObject("countRoot", this.hitCounter.getCountRoot());
		modelAndView.addObject("personCount", this.personRepository.count());
		
		return modelAndView;
	}
	
}
