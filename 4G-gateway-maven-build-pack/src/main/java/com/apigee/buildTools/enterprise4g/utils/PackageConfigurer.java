package com.apigee.buildTools.enterprise4g.utils;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import com.apigee.buildTools.enterprise4g.utils.ConfigTokens.Policy;
import com.apigee.cs.ast.scm.git.GitUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * updates the configuration values of a package
 * 
 * @author sdey
 */

public class PackageConfigurer {

	public static void configurePackage(String env, File configFile)
			throws Exception {

		Logger logger = LoggerFactory.getLogger(PackageConfigurer.class);

		TransformerFactory transformerFactory = TransformerFactory
				.newInstance();
		Transformer transformer = transformerFactory.newTransformer();

		// get the list of files in proxies folder
		XMLFileListUtil listFileUtil = new XMLFileListUtil();

		List<File> fileList = listFileUtil.getProxyFiles(configFile);
		FileReader fileutil = new FileReader();

		ConfigTokens conf = fileutil.getBundleConfigs(configFile);

		for (int i = 0; i < fileList.size(); i++) {

			Document xmlDoc = fileutil.getXMLDocument(fileList.get(i));

			try {
				Policy configTokens = conf.getConfigbyEnv(env)
						.getProxyFileNameMatch(fileList.get(i).getName());
				// logger.info("\n\n=============Replacing config tokens for Environment {}, for policy or proxy file name {}================\n",env,fileList.get(i).getName());
				if (configTokens != null) {
					logger.info(
							"=============Replacing config tokens for Environment {}, for proxy file name {}================\n",
							env, fileList.get(i).getName());
					xmlDoc = replaceTokens(xmlDoc, configTokens);
					DOMSource source = new DOMSource(xmlDoc);
					StreamResult result = new StreamResult(fileList.get(i));
					transformer.transform(source, result);
				}
			} catch (Exception e) {
				logger.error(
						"\n\n=============No config tokens found for Environment {}, for proxy file name {}================\n",
						env, fileList.get(i).getName());
				throw e;
			}

		}

		// get the list of files in policies folder
		fileList = listFileUtil.getPolicyFiles(configFile);
		for (int i = 0; i < fileList.size(); i++) {

			Document xmlDoc = fileutil.getXMLDocument(fileList.get(i));

			try {
				Policy configTokens = conf.getConfigbyEnv(env)
						.getPolicyFileNameMatch(fileList.get(i).getName());
				if (configTokens != null) {
					logger.info(
							"=============Replacing config tokens for Environment {}, for policy file name {}================\n",
							env, fileList.get(i).getName());
					xmlDoc = replaceTokens(xmlDoc, configTokens);
					DOMSource source = new DOMSource(xmlDoc);
					StreamResult result = new StreamResult(fileList.get(i));
					transformer.transform(source, result);
				}
			} catch (Exception e) {
				logger.error(
						"\n\n=============No config tokens found for Environment {}, for proxy file name {}================\n",
						env, fileList.get(i).getName());
				throw e;
			}

		}

		// get the list of files in targets folder
		fileList = listFileUtil.getTargetFiles(configFile);
		for (int i = 0; i < fileList.size(); i++) {

			Document xmlDoc = fileutil.getXMLDocument(fileList.get(i));

			try {
				Policy configTokens = conf.getConfigbyEnv(env)
						.getTargetFileNameMatch(fileList.get(i).getName());
				if (configTokens != null) {
					logger.info(
							"=============Replacing config tokens for Environment {}, for policy file name {}================\n",
							env, fileList.get(i).getName());
					xmlDoc = replaceTokens(xmlDoc, configTokens);
					DOMSource source = new DOMSource(xmlDoc);
					StreamResult result = new StreamResult(fileList.get(i));
					transformer.transform(source, result);
				}
			} catch (Exception e) {
				logger.error(
						"\n\n=============No config tokens found for Environment {}, for proxy file name {}================\n",
						env, fileList.get(i).getName());
				throw e;
			}

		}
		
		
		// update application metadata in the apiproxy folder
		
		// get the list of files in targets folder
		fileList = listFileUtil.getAPIProxyFiles(configFile);	
			
		Document xmlDoc = fileutil.getXMLDocument(fileList.get(0)); // there would be only one file, at least one file
		
		javax.xml.xpath.XPathFactory factory = javax.xml.xpath.XPathFactory.newInstance();
		javax.xml.xpath.XPath xpath = factory.newXPath();
						
				javax.xml.xpath.XPathExpression expression = xpath.compile("/APIProxy/Description");
				
				NodeList nodes = (NodeList) expression.evaluate(xmlDoc,
						XPathConstants.NODESET);
				
				if (nodes.item(0).hasChildNodes()){
					String orginialText = "";
					orginialText = nodes.item(0).getTextContent();
					nodes.item(0).setTextContent(getComment(fileList.get(0))+" ::"+orginialText);
					
				}
				else {
					nodes.item(0).setTextContent(getComment(fileList.get(0)));
				}
				
					
					DOMSource source = new DOMSource(xmlDoc);
					StreamResult result = new StreamResult(fileList.get(0));
					transformer.transform(source, result);
			

	}

	public static Document replaceTokens(Document doc, Policy configTokens)
			throws XPathExpressionException, TransformerConfigurationException {

		Logger logger = LoggerFactory.getLogger(PackageConfigurer.class);

		javax.xml.xpath.XPathFactory factory = javax.xml.xpath.XPathFactory
				.newInstance();
		javax.xml.xpath.XPath xpath = factory.newXPath();

		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		String json = gson.toJson(configTokens);
		logger.info(
				"============= to apply the following config tokens ================\n{}",
				json);

		// TransformerFactory transformerFactory =
		// TransformerFactory.newInstance();
		// Transformer transformer = transformerFactory.newTransformer();

		try {
			for (int i = 0; i < configTokens.tokens.size(); i++) {

				logger.debug(
						"=============Checking for Xpath Expressions {}  ================\n",
						configTokens.tokens.get(i).xpath);
				javax.xml.xpath.XPathExpression expression = xpath
						.compile(configTokens.tokens.get(i).xpath);
				// javax.xml.xpath.XPathExpression expression =
				// xpath.compile("/ProxyEndpoint/RouteRule/URL");

				NodeList nodes = (NodeList) expression.evaluate(doc,
						XPathConstants.NODESET);

				for (int j = 0; j < nodes.getLength(); j++) {

					if (nodes.item(j).hasChildNodes()) {
						logger.debug(
								"=============Updated existing value {} to new value {} ================\n",
								nodes.item(j).getTextContent(),
								configTokens.tokens.get(i).value);
						nodes.item(j).setTextContent(
								configTokens.tokens.get(i).value);
						// nodes.item(j)
						// .setTextContent("Hardcoded value");

						// logger.info("Node content{}",
						// nodes.item(j).getTextContent());
					}
					// if (nodes.item(j).hasAttributes())
					// {
					//
					// nodes.item(j).getAttributes().item(0).setTextContent(configTokens.tokens.get(i).value);
					// }
				}

			}

			// DOMSource source = new DOMSource(doc);
			// StreamResult result = new StreamResult(new
			// File("/Users/santanudey/Projects/4G/code-fest/project/target/apiproxy/proxies/new.xml"));
			// transformer.transform(source, result);

			return doc;
		} catch (Exception e) {

			logger.error(
					"\n\n=============The Xpath Expressions in config.json are incorrect. Please check. ================\n\n{}",
					e.getMessage());
			throw (XPathExpressionException) e;
		}

	}
	
	protected static String getComment(File basePath) {
		try {
			String hostname = "unknown";
			String user = System.getProperty("user.name", "unknown");
			try {
				hostname = InetAddress.getLocalHost().getHostName();

			} catch (UnknownHostException e) {
			}
			return user + " " + getScmRevision(basePath) + " " + hostname;
		} catch (Throwable t) {
			// If this blows up, continue on....
			return "";
		}
	}
	
	protected static String getScmRevision(File basePath ) {
		String rev = null;
		try {
			com.apigee.cs.ast.scm.git.GitUtil gu = new com.apigee.cs.ast.scm.git.GitUtil(basePath);
			String tagName = gu.getTagNameForWorkspaceHeadRevision();
			rev = "git: ";
			rev = (tagName == null) ? rev + "" : rev + tagName + " - "; 
			String revNum = gu.getWorkspaceHeadRevisionString();
			revNum = revNum.substring(0, Math.min(revNum.length(), 8));
			rev = rev + revNum ;

		} catch (Throwable e) {
			rev = null;
		}
		if (rev == null) {
			rev = "git revision unknown";
		}

		return rev;
	}

}
