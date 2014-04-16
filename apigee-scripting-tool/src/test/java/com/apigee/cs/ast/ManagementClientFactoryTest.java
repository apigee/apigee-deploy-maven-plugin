package com.apigee.cs.ast;

import org.junit.Test;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;

import junit.framework.Assert;

public class ManagementClientFactoryTest {

	@Test
	public void testRestSetupSecure() {

		GatewayEndpointConfig ep = new GatewayEndpointConfig();
		ep.setHostname("myhost");
		ep.setUsername("myuser");
		ep.setPassword("mypass");
		ep.setSecure(true);
		ep.setPort(9081);
		SimpleGatewayEndpointConfigLookup ecl = new SimpleGatewayEndpointConfigLookup();
		ecl.setStaticEndpointConfig(ep);

		ManagementClientFactory mcf = new ManagementClientFactory();

		mcf.setEndpointConfigLookup(ecl);

		WebResource wr = mcf.getSoiRestWebResource();

		Assert.assertEquals("https://myhost:9081/soi/api", wr.toString());

	}

	@Test
	public void testRestSetupPlain() {

		GatewayEndpointConfig ep = new GatewayEndpointConfig();
		ep.setHostname("somehost");
		ep.setUsername("someuser");
		ep.setPassword("somepass");

		SimpleGatewayEndpointConfigLookup ecl = new SimpleGatewayEndpointConfigLookup();
		ecl.setStaticEndpointConfig(ep);

		ManagementClientFactory mcf = new ManagementClientFactory();

		mcf.setEndpointConfigLookup(ecl);

		WebResource wr = mcf.getSoiRestWebResource();

		Assert.assertEquals("http://somehost:9080/soi/api", wr.toString());

	}
}
