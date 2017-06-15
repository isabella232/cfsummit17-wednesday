package demo.pcf.servicebroker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.cloud.servicebroker.model.Catalog;
import org.springframework.cloud.servicebroker.model.DashboardClient;
import org.springframework.cloud.servicebroker.model.Plan;
import org.springframework.cloud.servicebroker.model.ServiceDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * <P>The starting point here is the {@code Catalog} @Bean 
 * which lists what the service broker can build as services.
 * </P>
 */
@Configuration
public class CatalogConfig {

	@Bean
	public Catalog catalog(ServiceDefinition serviceDefinition) {
		List<ServiceDefinition> serviceDefinitions = new ArrayList<>();
		serviceDefinitions.add(serviceDefinition);
		return new Catalog(serviceDefinitions);
	}

	// Only define one machine sizing plan
	@Bean
	public Plan plan() {
		// Not sure if this metadata is needed
		// https://docs.cloudfoundry.org/services/catalog-metadata.html#plan-metadata-fields
		Map<String, Object> metadata = new HashMap<>();
		
		return new Plan(
				/* Id */
				"plan1",
				/* Name */
				"plan1",
				/* Description */
				"The only plan",
				/* Metadata */
				metadata,
				/* Is Free */
				true
				);
	}

	@Bean
	public ServiceDefinition serviceDefinition(Plan plan) {
		List<Plan> plans = new ArrayList<>();
		plans.add(plan);
		
		List<String> tags = Arrays.asList("hazelcast");
		
		// Used by the web console, we can skip
		// https://docs.cloudfoundry.org/services/catalog-metadata.html#services-metadata-fields
		Map<String, Object> metadata = new HashMap<>();

		// No required permissions
		List<String> requiredPermissions = null;
		// No dashboard client
		DashboardClient dashboardClient = null;
		
		return new ServiceDefinition(
						/* Id */
						"hazelcast-service-broker",
						/* Name */
						"hazelcast",
						/* Description */
						"Hazelcast Server",
						/* Bindable */
						true,
						/* PlanUpdateable */
						false,
						/* Plans */
						plans,
						/* Tags */
						tags,
						/* Metadata */
						metadata,
						/* Requires */
						requiredPermissions,
						/* DashboardClient */
						dashboardClient
						);
	}
	
}
