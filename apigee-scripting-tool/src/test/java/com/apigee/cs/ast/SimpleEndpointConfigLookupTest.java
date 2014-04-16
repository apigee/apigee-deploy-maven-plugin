package com.apigee.cs.ast;

import org.junit.Test;

import junit.framework.Assert;

/**
 * Unit test for simple App.
 */
public class SimpleEndpointConfigLookupTest

{
	@org.junit.Test
	public void testEnvironment() {
		System.setProperty("some.test.property", "My Value");
		Assert.assertEquals("My Value", System.getProperty("some.test.property"));

		SimpleGatewayEndpointConfigLookup lookup = new SimpleGatewayEndpointConfigLookup();

		Assert.assertEquals("My Value", lookup.getConfigLoader().getValue("some.test.property"));
		Assert.assertNull(lookup.getConfigLoader().getValue("SOME_TEST_PROPERTY"));
	}

	@Test
	public void testPomLoader() {
		try {
			System.setProperty("ast.pom", "./src/test/resources/test-pom.xml");
			System.setProperty("ast.profile", "test-2");

			SimpleGatewayEndpointConfigLookup lookup = new SimpleGatewayEndpointConfigLookup();

			Assert.assertEquals("secret2", lookup.getConfigLoader().getValue("ast.password"));

		} finally {
			System.setProperty("ast.pom", "");
			System.setProperty("ast.profile", "");
		}
	}
}
