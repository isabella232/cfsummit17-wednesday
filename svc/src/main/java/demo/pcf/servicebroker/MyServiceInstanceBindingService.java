package demo.pcf.servicebroker;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.cloud.servicebroker.model.CreateServiceInstanceAppBindingResponse;
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceBindingRequest;
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceBindingResponse;
import org.springframework.cloud.servicebroker.model.DeleteServiceInstanceBindingRequest;
import org.springframework.cloud.servicebroker.service.ServiceInstanceBindingService;
import org.springframework.stereotype.Service;

/**
 * <P>Actions necessary to bind the application to the service, none!
 * </P>
 */
@Service
public class MyServiceInstanceBindingService implements ServiceInstanceBindingService {

	private static final String LOCALHOST = "127.0.0.1";
	
	@Override
	public CreateServiceInstanceBindingResponse createServiceInstanceBinding(CreateServiceInstanceBindingRequest arg0) {

		Map<String,Object> info = new HashMap<>();
		String host = MyServiceInstanceBindingService.findHostRemoteIp();
		info.put("host", host);
	
		if (host.equals(LOCALHOST)) {
			throw new RuntimeException("Host derived as " + host + " cannot use remotely");
		}
		
		//TODO: These aren't credentials
		return new CreateServiceInstanceAppBindingResponse().withCredentials(info);
	}

	@Override
	public void deleteServiceInstanceBinding(DeleteServiceInstanceBindingRequest arg0) {
	}

	private static String findHostRemoteIp() {
    	try {
			for (NetworkInterface networkInterface : Collections.list(NetworkInterface.getNetworkInterfaces())) {
				for (InetAddress inetAddress : Collections.list(networkInterface.getInetAddresses())) {
					if (inetAddress instanceof Inet4Address) {
						if (!LOCALHOST.equals(inetAddress.getHostAddress())) {
							return inetAddress.getHostAddress();
						}
					}
		        }				
			}
		} catch (SocketException e) {
			e.printStackTrace();
		}
		return LOCALHOST;
    }
}
