package com.apigee.cs.ast;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.transform.dom.DOMResult;

import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Node;

import com.apigee.cs.util.XPathUtil;

public class EndpointConfigJaxbTest {

	@Test
	public void testMe() throws Exception {
		JAXBContext ctx = JAXBContext.newInstance(new Class[] {GatewayEndpointConfig.class, GatewayEndpointConfigCollection.class});
		
		GatewayEndpointConfig cfg = new GatewayEndpointConfig();
		cfg.setHostname("hello");
		cfg.setPassword("mypass");
		cfg.setPort(1234);
		cfg.setSshPassword("sshpass");
		cfg.setSshUsername("admin");
		cfg.setUsername("adminuser");
		cfg.setId("myid");
		cfg.setSshPort(2233);
		Marshaller marshaller = ctx.createMarshaller();
		// the property JAXB_FORMATTED_OUTPUT specifies whether or not the
		// marshalled XML data is formatted with linefeeds and indentation
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		// marshal the data in the Java content tree
		// to the XML instance document niceVet.xml

		DOMResult dr = new DOMResult();
		marshaller.marshal(cfg, dr);

		XPathUtil xpu = new XPathUtil();
		xpu.addNamespace("a", XmlConstants.NS);
		Node n = dr.getNode();
		Assert.assertEquals(cfg.getHostname(),
				xpu.evalString(n, "//a:gatewayEndpointConfig/a:hostname/text()"));
		Assert.assertEquals(cfg.getPassword(),
				xpu.evalString(n, "//a:gatewayEndpointConfig/a:password/text()"));
		Assert.assertEquals(new String("" + cfg.getPort()),
				xpu.evalString(n, "//a:gatewayEndpointConfig/a:port/text()"));
		Assert.assertEquals(cfg.getSshUsername(),
				xpu.evalString(n, "//a:gatewayEndpointConfig/a:sshUsername/text()"));
		Assert.assertEquals(cfg.getSshPassword(),
				xpu.evalString(n, "//a:gatewayEndpointConfig/a:sshPassword/text()"));
		Assert.assertEquals(new String("" + cfg.getSshPort()),
				xpu.evalString(n, "//a:gatewayEndpointConfig/a:sshPort/text()"));
		Assert.assertEquals(cfg.getId(),
				xpu.evalString(n, "//a:gatewayEndpointConfig/@id"));
		
		GatewayEndpointConfigCollection cc = new GatewayEndpointConfigCollection();
		cc.add(cfg);
		marshaller.marshal(cc, System.out);
	}
}
