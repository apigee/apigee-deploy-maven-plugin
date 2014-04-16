package com.apigee.cs.ast;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.apigee.cs.util.XPathUtil;

public class SimpleGatewayEndpointConfigLookup implements
		GatewayEndpointConfigLookup {

	private Logger log = LoggerFactory.getLogger(SimpleGatewayEndpointConfigLookup.class);

	private GatewayEndpointConfig staticEndpointConfig = null;
	private ConfigLoader configLoader;
	public SimpleGatewayEndpointConfigLookup() {
		super();
		this.configLoader = new ConfigLoader();
	}

	@Override
	public GatewayEndpointConfig lookupEndpointConfig() {

		
		if (staticEndpointConfig != null) {
			// If someone has set a static endpoint config, use that
			return staticEndpointConfig;
		} else {
			// Otherwise build it up from environment properties
			GatewayEndpointConfig ep = new GatewayEndpointConfig();
			ep.setHostname(configLoader.getValue("AST_HOSTNAME"));
			ep.setUsername(configLoader.getValue("AST_USERNAME"));
			ep.setPassword(configLoader.getValue("AST_PASSWORD"));
			ep.setPort(Integer.parseInt(configLoader.getValue("AST_PORT", "9080")));
			ep.setSecure(Boolean.parseBoolean(configLoader.getValue("AST_SECURE", "false")));

			return ep;
		}
	}

	

	public void setStaticEndpointConfig(GatewayEndpointConfig ec) {
		staticEndpointConfig = ec;
	}
	public ConfigLoader getConfigLoader() {
		return configLoader;
	}

}
