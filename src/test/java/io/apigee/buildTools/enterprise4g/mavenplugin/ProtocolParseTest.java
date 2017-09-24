package io.apigee.buildTools.enterprise4g.mavenplugin;

import static org.junit.Assert.*;

import org.junit.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProtocolParseTest {

	//	Pattern pattern = Pattern.compile("^(http[s]?)://(.*)");
	Pattern pattern = GatewayAbstractMojo.URL_PARSE_REGEX;

	@Test
	public void testNoProto() {
		String host = "api.server.com";
		Matcher m = pattern.matcher(host);
		assertFalse(m.matches());
	}

	@Test
	public void testHttpProto() {
		String host = "http://api.server.com";
		Matcher m = pattern.matcher(host);
		assertTrue(m.matches());
	}

	@Test
	public void testHttpsProto() {
		String host = "https://api.server.com";
		Matcher m = pattern.matcher(host);
		assertTrue(m.matches());
	}

	@Test
	public void testExtractHttpsProto() {
		String host = "https://api.server.com";
		Matcher m = pattern.matcher(host);
		assertTrue(m.matches());
		assertEquals(2, m.groupCount());
		assertEquals("https", m.group(1));
	}

	@Test
	public void testExtractHttpProto() {
		String host = "http://api.server.com";
		Matcher m = pattern.matcher(host);
		assertTrue(m.matches());
		assertEquals(2, m.groupCount());
		assertEquals("http", m.group(1));
	}


	@Test
	public void testExtractDomain() {

		String domain = "api.server.com";
		Matcher m = pattern.matcher("http://" + domain);
		assertTrue(m.matches());
		assertEquals(2, m.groupCount());
		assertEquals(domain, m.group(2));

		domain = "api-server.server.com";
		m = pattern.matcher("http://" + domain);
		assertTrue(m.matches());
		assertEquals(2, m.groupCount());
		assertEquals(domain, m.group(2));

		domain = "api-server.server.com";
		m = pattern.matcher("http://" + domain + ":8080");
		assertTrue(m.matches());
		assertEquals(2, m.groupCount());
		assertEquals(domain, m.group(2));

		domain = "api-server.server.com";
		m = pattern.matcher("http://" + domain + ":8080/tst/url");
		assertTrue(m.matches());
		assertEquals(2, m.groupCount());
		assertEquals(domain, m.group(2));

		domain = "api-server.server.com";
		m = pattern.matcher("http://" + domain + "?tst/url");
		assertTrue(m.matches());
		assertEquals(2, m.groupCount());
		assertEquals(domain, m.group(2));

	}

}
