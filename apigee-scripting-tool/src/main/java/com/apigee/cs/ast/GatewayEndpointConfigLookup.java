package com.apigee.cs.ast;

/**
 * Lookup-pattern interface for obtaining endpoint config.
 * @author rob
 *
 */
public interface GatewayEndpointConfigLookup {

	public GatewayEndpointConfig lookupEndpointConfig();
	
}
