package com.apigee.cs.ast;

import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import junit.framework.Assert;

import org.junit.Test;

public class ConfigLoaderTest {

	@Test
	public void testSystemProperties() {

		// System properties have the highest precedence.
		// Here we just verify that we can read all the system properties AS IS
		ConfigLoader cl = new ConfigLoader();
		Properties props = System.getProperties();
		for (Map.Entry<Object, Object> me : props.entrySet()) {
			String key = (String) me.getKey();
			String val = (String) me.getValue();
			String valFromConfigLoader = cl.getValue(key);
			Assert.assertEquals(val, valFromConfigLoader);
		}
	}

	/**
	 * Test retreiving environmental variables. Env variables have a lower
	 * precedence than system properties, so we take care to make sure that we
	 * don't fail on a collision.
	 */
	@Test
	public void testGetEnvironmentVariables() {
		ConfigLoader cl = new ConfigLoader();
		Map<String, String> map = System.getenv();
		for (Map.Entry<String, String> me : map.entrySet()) {
			String key = me.getKey();
			String expectedValue = me.getValue();
		
			String systemPropertyValue = System.getProperty(key);
			if (systemPropertyValue != null) {
				expectedValue = systemPropertyValue;
			}
			String valFromConfigLoader = cl.getValue(key);
			Assert.assertEquals(expectedValue, valFromConfigLoader);
		}

	
	}

	@Test
	public void testOverride() {
		final String USER = "USER";
		String userVal = System.getenv(USER);
		try {

			ConfigLoader cl = new ConfigLoader();

			String newVal = UUID.randomUUID().toString();
			System.setProperty(USER, newVal);
			Assert.assertEquals(newVal, cl.getValue(USER));
		} finally {
			if (userVal != null) {
				System.setProperty(USER, userVal);

			}
		}
	}
	
	@Test
	public void testPomParsing() {
		
		System.setProperty("ast.pom", "src/test/resources/test-pom.xml");
		System.setProperty("ast.profile", "notfound");
		ConfigLoader cl = new ConfigLoader();
		// Just note that setting ast.profile=notfound doesn't throw an exception
		
		// Now set it to a property that is found
		System.setProperty("ast.profile", "test-1");
		cl = new ConfigLoader();
		
		Assert.assertEquals("192.168.0.1", cl.getValue("ast.hostname"));
	}

}
