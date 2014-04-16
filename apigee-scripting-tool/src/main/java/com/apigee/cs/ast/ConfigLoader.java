package com.apigee.cs.ast;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.apigee.cs.util.XPathUtil;

/**
 * ConfigLoader loads configuration from a variety of mechanisms.  In order of priority:
 * 1) From Java System properties.
 * 2) From an environment variable
 * 3) From reading a maven pom
 * 
 * Note that we are NOT using maven to read the pom.xml.  It just so happens that if you already have
 * profiles defined in pom.xml, it is handy to be able to leverage them from ast.
 * @author rob
 *
 */
public class ConfigLoader {

	boolean usePomProfileProperties = true;

	Properties pomProperties = null;

	Logger log = LoggerFactory.getLogger(ConfigLoader.class);

	public ConfigLoader() {
		loadFromPom();
	}

	public String getValue(String var) {
		return getValue(var, null);
	}

	public boolean isUsePomProfileProperties() {
		return usePomProfileProperties;
	}

	public void setUsePomProfileProperties(boolean usePomProfileProperties) {
		this.usePomProfileProperties = usePomProfileProperties;
	}

	public String getValue(String var, String defaultVal) {

		// First check system properties
		String s = getConvertedSystemProperty(var);
		if (s == null) {
			// Now fall back to environmental variables
			s = System.getenv(var);
			if (s == null) {

				if (isUsePomProfileProperties()) {
					s = pomProperties.getProperty(var.toLowerCase().replace(
							"_", "."));
				}
				if (s == null) {
					s = defaultVal;
				}
			}
		}
		return s;
	}

	String getConvertedSystemProperty(String key) {
		if (key.startsWith("AST_")) {
			key = key.toLowerCase().replace("_", ".");
		}
		return System.getProperty(key);

	}

	/**
	 * Load properties from a pom file. Although the maven plugin depends on
	 * this project and not vice versa, it is still useful to be able to point
	 * to a pom file and a profile and extract the properties.
	 */
	private void loadFromPom() {

		try {
			this.pomProperties = new Properties();
			String pomFileName = getValue("AST_POM");
			String profileName = getValue("AST_PROFILE");

			if (pomFileName == null || pomFileName.trim().length() == 0) {
				return;
			}
			File pomFile = new File(pomFileName);

			if (!pomFile.exists()) {
				log.warn("File does not exist: " + pomFileName);
				return;
			}

			if (profileName == null || profileName.trim().length() == 0) {
				log.warn("you specified a pom file, but didn't specify AST_PROFILE or -Dast.profile");
				return;
			}

			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setNamespaceAware(true);

			Document doc = dbf.newDocumentBuilder().parse(pomFile);

			XPathUtil xpu = new XPathUtil();
			xpu.addNamespace("m", "http://maven.apache.org/POM/4.0.0");

			Element el = (Element) xpu.evalNode(doc,
					"//m:profile[./m:id/text()='" + profileName
							+ "']/m:properties");

			if (el == null) {
				log.warn("profile '" + profileName + "' not found in "
						+ pomFile.getCanonicalPath());
				return;
			}
			NodeList nl = el.getChildNodes();

			for (int i = 0; i < nl.getLength(); i++) {
				Node n = nl.item(i);
				if (n instanceof Element) {
					String pname = n.getNodeName();
					String pval = ((Element) n).getTextContent();

					pomProperties.setProperty(pname, pval);
				}
			}
		

		} catch (IOException e) {
			throw new AstException(e);
		} catch (SAXException e) {
			throw new AstException(e);
		} catch (ParserConfigurationException e) {
			throw new AstException(e);
		}
	}
}
