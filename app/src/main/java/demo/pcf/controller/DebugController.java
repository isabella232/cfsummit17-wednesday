package demo.pcf.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hazelcast.core.HazelcastInstance;

import demo.pcf.HitCounter;
import lombok.Data;

@RestController
@RequestMapping("debug")
public class DebugController {

	@Autowired
	@Qualifier("addresses")
	String[] addresses;

	@Autowired
	Environment environment;
	
	@Autowired
	private HazelcastInstance hazelcastInstance;
	
	@Autowired
	HitCounter hitCounter;
		
	@Data
	static class DebugInfo {
		List<String> original;
		List<String> current;
	}
	
	@GetMapping(value="addresses",
				produces = MediaType.APPLICATION_JSON_VALUE)
	public DebugInfo addresses() {
		this.hitCounter.hitAnyPage();
		
		DebugInfo debugInfo = new DebugInfo();
		debugInfo.setOriginal(Arrays.asList(addresses));
		debugInfo.setCurrent(new ArrayList<>());
		
		this.hazelcastInstance.getCluster().getMembers()
		.forEach(member -> debugInfo.getCurrent().add(member.getAddress().getHost()));
		
		return debugInfo;
	}
	
	@GetMapping(value="vcap", 
				produces = MediaType.APPLICATION_JSON_VALUE)
	public String vcap() {
		this.hitCounter.hitAnyPage();
		String DEFAULT = "{\"not\" : \"set\"}";
		return this.environment.getProperty("VCAP_SERVICES", DEFAULT);
	}
	
}
