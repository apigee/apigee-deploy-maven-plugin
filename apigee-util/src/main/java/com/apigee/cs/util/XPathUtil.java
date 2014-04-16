package com.apigee.cs.util;

import javax.xml.namespace.QName;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XPathUtil {

	NamespaceMap map = new NamespaceMap();

	public void setNamespaceMap(NamespaceMap map) {
		this.map = map;
	}

	public void addNamespace(String prefix, String uri) {
		map.addNamespace(prefix, uri);
	}

	public String evalString(Node n, String xpath) {
		return (String) evalXPath(n, xpath, XPathConstants.STRING, map);
	}

	public boolean evalBoolean(Node n, String xpath) {
		Boolean b = (Boolean) evalXPath(n, xpath, XPathConstants.BOOLEAN, map);
		return b;
	}
	public Node evalNode(Node n, String xpath) {
		return (Node) evalXPath(n,xpath,XPathConstants.NODE,map);
	}
	public NodeList evalNodeList(Node n, String xpath) {
		return (NodeList) evalXPath(n,xpath,XPathConstants.NODESET,map);
	}
	public Object evalXPath(Node n, String xpathExpression, QName type,
			NamespaceMap namespaceMap) {
		try {
			XPath xpath = XPathFactory.newInstance().newXPath();
			if (namespaceMap != null) {
				xpath.setNamespaceContext(namespaceMap);
			}
			XPathExpression compiledExpression = xpath.compile(xpathExpression);
			return compiledExpression.evaluate(n, type);
		} catch (XPathExpressionException e) {
			throw new EvaluationException(e);
		}
	}

}
