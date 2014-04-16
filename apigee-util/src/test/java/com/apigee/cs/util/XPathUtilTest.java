package com.apigee.cs.util;

import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import junit.framework.Assert;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class XPathUtilTest {

	
	@Test
	public void testXpath() throws ParserConfigurationException, TransformerConfigurationException, TransformerException, TransformerFactoryConfigurationError {
		Document d = getSampleDocument();
		
		XPathUtil u = new XPathUtil();
		u.addNamespace("x", "urn:test");
		

		Assert.assertEquals("David Foster Wallace", u.evalString(d, "//x:book[1]/x:author/text()"));
		Assert.assertEquals("true", u.evalString(d,"//x:book[1]/x:author/@deceased"));
		Assert.assertTrue( u.evalBoolean(d,"//x:book[1]/x:author/@deceased"));
		
		Assert.assertFalse( u.evalBoolean(d,"//x:book[1]/x:author/@invalid"));
		
	}
	
	
	@Test
	public void testStreamAndReparse() throws TransformerConfigurationException, TransformerException, TransformerFactoryConfigurationError, ParserConfigurationException {
		StringWriter sw = new StringWriter();
		TransformerFactory.newInstance().newTransformer().transform(new DOMSource(getSampleDocument()), new StreamResult(sw));
		
		Document d = new DOMUtil().parseXmlString(sw.toString());
		
		TransformerFactory.newInstance().newTransformer().transform(new DOMSource(getSampleDocument()), new StreamResult(System.out));
		
		TransformerFactory.newInstance().newTransformer().transform(new DOMSource(d), new StreamResult(System.out));
		
		XPathUtil u = new XPathUtil();
		u.addNamespace("x", "urn:test");
		

		Assert.assertEquals("David Foster Wallace", u.evalString(d, "//x:book[1]/x:author/text()"));
		Assert.assertEquals("true", u.evalString(d,"//x:book[1]/x:author/@deceased"));
		Assert.assertTrue( u.evalBoolean(d,"//x:book[1]/x:author/@deceased"));
		
		Assert.assertFalse( u.evalBoolean(d,"//x:book[1]/x:author/@invalid"));
	}
	Document getSampleDocument() throws ParserConfigurationException {
		Document d = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
		String ns1 = "urn:test";
		Element e = d.createElementNS(ns1, "books");
		d.appendChild(e);
		
		
		Element book = d.createElementNS(ns1,"book");
		Element author = d.createElementNS(ns1, "author");
		author.setAttribute("deceased", "true");
		author.appendChild(d.createTextNode("David Foster Wallace"));
		Element title = d.createElementNS(ns1,"title");
		title.appendChild(d.createTextNode("Infinte Jest"));
		book.appendChild(title);
		book.appendChild(author);
		e.appendChild(book);
		
		
		 book = d.createElementNS(ns1,"book");
		 author = d.createElementNS(ns1, "author");
		author.appendChild(d.createTextNode("Ernest Hemmingway"));
		 title = d.createElementNS(ns1,"title");
		title.appendChild(d.createTextNode("The Sun Also Rises"));
		book.appendChild(title);
		book.appendChild(author);
		e.appendChild(book);
		return d;
	}
}
