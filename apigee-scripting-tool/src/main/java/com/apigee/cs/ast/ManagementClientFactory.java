package com.apigee.cs.ast;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sonoa.security.util.EncryptionUtil;
import com.sonoa.services.alerts.spi.AlertService;
import com.sonoa.soiclient.BaseServiceClient;
import com.sonoa.soiclient.configuration.ConfigurationServiceClient;
import com.sonoa.soiclient.management.ClusterManagementServiceClient;
import com.sonoa.soiclient.management.NodeManagementServiceClient;
import com.sonoa.soiclient.monitor.MonitorServiceClient;
import com.sonoa.soiclient.security.SecurityServiceClient;
import com.sonoa.soiclient.security.UserManagementServiceClient;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import com.sun.jersey.api.client.filter.LoggingFilter;

public class ManagementClientFactory implements GatewayEndpointConfigLookup {
	Logger log = LoggerFactory.getLogger(ManagementClientFactory.class);
	public static final int DEFAULT_TIMEOUT=60000;
	private int timeout = 60000;
	
	GatewayEndpointConfigLookup endpointResolver = new SimpleGatewayEndpointConfigLookup();
	ConfigLoader configLoader = new ConfigLoader();
	public ManagementClientFactory(){
		
		
		timeout = Integer.parseInt(configLoader.getValue("AST_TIMEOUT",Integer.toString(DEFAULT_TIMEOUT)));
	}
	

	
	public void setEndpointConfigLookup(GatewayEndpointConfigLookup ecl) {
		endpointResolver = ecl;
	}

	String createUrl(GatewayEndpointConfig endpoint, String basePath) {
		String url = lookupEndpointConfig().getManagementUrl() + basePath;
		log.debug("Using URL: " + url);

		return url;
	}

	public final GatewayEndpointConfig lookupEndpointConfig() {
		GatewayEndpointConfig ep = endpointResolver.lookupEndpointConfig();
		ep.validateConfig();
		return ep;
	}

	public UserManagementServiceClient getUserManagementServiceClient() {

		return (UserManagementServiceClient) getServiceClient(
				createUrl(lookupEndpointConfig(),
						"/soi/services/UserManagementService"), getTimeout());

	}

	public ConfigurationServiceClient getConfigurationServiceClient() {

		return (ConfigurationServiceClient) getServiceClient(
				createUrl(lookupEndpointConfig(),
						"/soi/services/ConfigurationService"), getTimeout());

	}

	public SecurityServiceClient getSecurityServiceClient() {

		SecurityServiceClient c = (SecurityServiceClient) getServiceClient(
				createUrl(lookupEndpointConfig(),
						"/soi/services/SecurityService"), getTimeout());

		return c;

	}

	public NodeManagementServiceClient getNodeManagementServiceClient() {
		NodeManagementServiceClient c = (NodeManagementServiceClient) getServiceClient(
				createUrl(lookupEndpointConfig(),
						"/soi/services/NodeManagementService"), getTimeout());
		return c;
	}
	public ClusterManagementServiceClient getClusterManagementServiceClient() {
		ClusterManagementServiceClient c = (ClusterManagementServiceClient) getServiceClient(
				createUrl(lookupEndpointConfig(),
						"/soi/services/ClusterManagementService"), getTimeout());
		return c;
	}
	
	public MonitorServiceClient getMonitorServiceClient() {
		MonitorServiceClient c = (MonitorServiceClient) getServiceClient(
				createUrl(lookupEndpointConfig(),
						"/soi/services/MonitorService"), getTimeout());
		return c;
	}
	

	protected void decorateAuthentication(BaseServiceClient client) {

		client.setUserName(lookupEndpointConfig().getUsername());
		client.setPassword(lookupEndpointConfig().getCTP());
	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public Client getSoiRestClient() {
		GatewayEndpointConfig ep = endpointResolver.lookupEndpointConfig();
		
		DefaultClientConfig cc = new DefaultClientConfig();
		Client c = Client.create(cc);
		c.addFilter(new LoggingFilter());
		c.addFilter(new HTTPBasicAuthFilter(ep.getUsername(), ep.getPassword()));
		return c;
	}
	public WebResource getSoiRestWebResource() {
		String url = endpointResolver.lookupEndpointConfig().getManagementUrl();
	
		WebResource wr = getSoiRestClient().resource(url).path("soi").path("api");
		return wr;
	}
	public BaseServiceClient getServiceClient(String serviceUrl, int wsTimeout) {
		try {

			BaseServiceClient client = null;
			if (serviceUrl.endsWith("ConfigurationService"))
				client = new ConfigurationServiceClient(serviceUrl, wsTimeout);
			else if (serviceUrl.endsWith("ClusterManagementService"))
				client = new ClusterManagementServiceClient(serviceUrl,
						wsTimeout);
			else if (serviceUrl.endsWith("NodeManagementService"))
				client = new NodeManagementServiceClient(serviceUrl, wsTimeout);
			else if (serviceUrl.endsWith("MonitorService"))
				client = new MonitorServiceClient(serviceUrl, wsTimeout);
			else if (serviceUrl.endsWith("SecurityService"))
				client = new SecurityServiceClient(serviceUrl, wsTimeout);
			else if (serviceUrl.endsWith("UserManagementService")) {
				client = new UserManagementServiceClient(serviceUrl, wsTimeout);
			} else {
				throw new IllegalStateException(serviceUrl);
			}
			decorateAuthentication(client);
			return client;
		} catch (AstException e) {
			throw e;
		} catch (RuntimeException e) {
			throw new AstException(e);
		} catch (Exception e) {
			throw new AstException(e);
		}
	}

	
}
