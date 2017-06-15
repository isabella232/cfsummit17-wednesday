package demo.pcf;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.json.BasicJsonParser;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.hazelcast.HazelcastKeyValueAdapter;
import org.springframework.data.hazelcast.repository.config.EnableHazelcastRepositories;
import org.springframework.data.keyvalue.core.KeyValueTemplate;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;

import lombok.extern.slf4j.Slf4j;

@Configuration
@EnableHazelcastRepositories
@Slf4j
public class ApplicationConfig {

	/**
	 * <P>Code stolen from 
	 * {@link https://github.com/hazelcast/hazelcast-code-samples/blob/master/hazelcast-integration/pcf-integration/src/main/java/com/hazelcast/pcf/integration/Application.java}
	 * </P>
	 * <P>Find the IP addresses of the Hazelcast servers this client should connect to.
	 * </P>
	 * 
	 * @param environment
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Bean(name="addresses")
	public String[] addresses(Environment environment) {
		String[] LOCALHOST = new String[] {"127.0.0.1"};

		String servicesJson = environment.getProperty("VCAP_SERVICES");
		if (servicesJson==null || servicesJson.length()==0) {
			log.info("VCAP_SERVICES is null");
			return LOCALHOST;
		} else {
			log.info("VCAP_SERVICES='{}'", servicesJson);
		}
		
		BasicJsonParser parser = new BasicJsonParser();
		Map<String, Object> json = parser.parseMap(servicesJson);
		List hazelcast = (List) json.get("hazelcast");
		
		if (hazelcast!=null) {
			// Tile
			Map map = (Map) hazelcast.get(0);
			Map credentials = (Map) map.get("credentials");
			List<String> members = (List<String>) credentials.get("members");

			if (members!=null) {
				// Proper PCF Tile
				String[] results = new String[members.size()];
				
				for (int i=0; i<results.length; i++) {
					results[i] = members.get(i).replace('"',' ').trim();
				}
				
				return results;
			} else {
				// PCF Dev "svc"
				String host = (String) credentials.get("host");
				
				String[] results = new String[1];
				results[0] = host;
				
				return results;
			}
		} else {
			// Cups
			List userProvided = (List) json.get("user-provided");			
			Map map = (Map) userProvided.get(0);
			Map credentials = (Map) map.get("credentials");
			String host = credentials.get("host").toString();
			
			return new String[] { host };
		}
	}

	@Bean
	public ClientConfig clientConfig(@Qualifier("addresses") String[] addresses) {
		ClientConfig clientConfig = new ClientConfig();

		log.info("Connection list '{}'", Arrays.asList(addresses));
		clientConfig.getNetworkConfig().addAddress(addresses);

		return clientConfig;
	}
	
	@Bean
    public HazelcastInstance hazelcastInstance(ClientConfig clientConfig) {
		return HazelcastClient.newHazelcastClient(clientConfig);
    }

    // For Spring Data Hazelcast
	@Bean
	public HazelcastKeyValueAdapter hazelcastKeyValueAdapter(HazelcastInstance hazelcastInstance) {
    	return new HazelcastKeyValueAdapter(hazelcastInstance);
	}

    // For Spring Data Hazelcast
	@Bean
	public KeyValueTemplate keyValueTemplate(HazelcastKeyValueAdapter hazelcastKeyValueAdapter) {
		return new KeyValueTemplate(hazelcastKeyValueAdapter);
    }
    
}
