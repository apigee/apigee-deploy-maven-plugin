package com.apigee.cs.util;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class DOMUtil {

	public Document parseXmlString(String s) {
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setNamespaceAware(true);
			
		return dbf.newDocumentBuilder().parse(new InputSource(new StringReader(s)));
		}
		catch (SAXException e) {
			throw new EvaluationException(e);
		}
		catch (ParserConfigurationException e) {
			throw new EvaluationException(e);
		}
		catch (IOException e) {
			throw new EvaluationException(e);
		}
	
	}
	
	public static final List<Node> getChildElements(Node n){
		List<Node> childElementList = new ArrayList<Node>();
		NodeList childNodes = n.getChildNodes();
		if(childNodes != null){
			for(int i=0;i<childNodes.getLength();i++){
				Node child = childNodes.item(i);
				if(child.getNodeType() == Node.ELEMENT_NODE){
					childElementList.add(child);
				}
			}
		}
		return childElementList;
	}
	
	public static final Node getFirstChildElement(Document doc, String childElementName){
		NodeList childNodes = doc.getElementsByTagName(childElementName);
		if(childNodes != null && childNodes.getLength() > 0){
			return childNodes.item(0);
		}
		return null;
	}
	

	
	
	public static final Node mergeSubElements(Node a, Node b, Document bDoc, Node bLastNode, Node bParent){
		
		if(a == null && b == null){
			return bLastNode;
		}
		if(b == null && a != null){
			Node importedA = bDoc.importNode(a, true);
			if(bLastNode == null){
				bParent.appendChild(importedA);
			}else{
				bLastNode.getParentNode().insertBefore(importedA, bLastNode);
			}
			return importedA;
		 }
		if(b != null && a == null){
			 return b;
		 }
		
		List<Node> bChildElements = getChildElements(b);
		Node bTopChild =  bChildElements.isEmpty()? null:bChildElements.get(0);
		List<Node> aChildNodes = getChildElements(a);
		
		Collections.reverse(aChildNodes);
		for(Node aChild:aChildNodes){
			
			if(bTopChild == null){
				
				b.appendChild(bDoc.importNode(aChild, true));
			}else{
				b.insertBefore(bDoc.importNode(aChild, true), bTopChild);
			}
			bTopChild = b.getFirstChild();
		}
		return b;
	}
	
	public static final Node getChildElementWithName(Node parent, String elementName){
		List<Node> childNodes = getChildElements(parent);
		for(Node child:childNodes){
			if(child.getNodeName().equals(elementName)){
				return child;
			}
		}
		return null;
	}

	
	
	
}
