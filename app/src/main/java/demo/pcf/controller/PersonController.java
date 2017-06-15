package demo.pcf.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import demo.pcf.HitCounter;
import demo.pcf.Person;
import demo.pcf.PersonRepository;
import demo.pcf.ReflectionUtil;

@Controller
@RequestMapping("person")
public class PersonController {

	@Autowired
	HitCounter hitCounter;
	@Autowired
	PersonRepository personRepository;
		
	@GetMapping("list")
	public ModelAndView list() {
		this.hitCounter.hitAnyPage();

		ModelAndView modelAndView = new ModelAndView("person/list");
		
        modelAndView.addObject("columns", ReflectionUtil.getColumns(Person.class));
        modelAndView.addObject("data", ReflectionUtil.getData(this.personRepository.findAll(), Person.class));
		
		return modelAndView;
	}
	
}
