package com.apigee.cs.util;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.namespace.NamespaceContext;

/**
 * Hello world!
 * 
 */
public class NamespaceMap implements NamespaceContext {
	Map<String,String> prefixUriMap = new ConcurrentHashMap<String, String>();
	Map<String,String> uriPrefixMap = new ConcurrentHashMap<String, String>();
	public void addNamespace(String prefix, String uri) {
		prefixUriMap.put(prefix,uri);
		uriPrefixMap.put(uri,prefix);
	}
	
	
	
	public String getNamespaceURI(String arg0) {
		String uri = prefixUriMap.get(arg0);
	
		return uri;
	}

	public String getPrefix(String arg0) {
		String prefix = uriPrefixMap.get(arg0);
		return prefix;
	}

	public Iterator getPrefixes(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}
}
