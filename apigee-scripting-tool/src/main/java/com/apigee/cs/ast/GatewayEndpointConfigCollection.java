package com.apigee.cs.ast;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Currently unused.  The thinking here is that we can use JAXB to serialize multiple GatewayEndpointConfigs.  This would allow for a simple way to, for instance say:
 *  Deploy this APP to this collection of gateways.
 * 
 * @author rob
 *
 */
@XmlRootElement(name="gatewayEndpointConfigCollection", namespace=XmlConstants.NS)
public class GatewayEndpointConfigCollection {

	@XmlElementWrapper(name="gateways", namespace=XmlConstants.NS)
	@XmlElement(name="gatewayEndpoiintConfig")
	ArrayList<GatewayEndpointConfig> list = new ArrayList<GatewayEndpointConfig>();
	
	public void add(GatewayEndpointConfig cc) {
		list.add(cc);
	}
}
