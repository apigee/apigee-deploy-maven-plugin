/**
 * Copyright (C) 2014 Apigee Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.apigee.buildTools.enterprise4g.utils;

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
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import io.apigee.buildTools.enterprise4g.utils.ConfigTokens.Policy;

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
                    //Fix for https://github.com/apigee/apigee-deploy-maven-plugin/issues/66
                    result.setSystemId(fileList.get(i).getAbsolutePath());
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
                    //Fix for https://github.com/apigee/apigee-deploy-maven-plugin/issues/66
                    result.setSystemId(fileList.get(i).getAbsolutePath());
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
                    //Fix for https://github.com/apigee/apigee-deploy-maven-plugin/issues/66
                    result.setSystemId(fileList.get(i).getAbsolutePath());
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

        Node node = (Node) expression.evaluate(xmlDoc, XPathConstants.NODE);
        if (node == null) {
            Element root = xmlDoc.getDocumentElement();
            node = xmlDoc.createElement("Description");
            root.appendChild(node);
        }

        if (node.hasChildNodes()) {
            // sets the description to whatever is in the <proxyname>.xml file
            node.setTextContent(expression.evaluate(xmlDoc));
        } else {
            // if Description is empty, then it reverts back to appending the username, git hash, etc
            node.setTextContent(getComment(fileList.get(0)));
        }

        DOMSource source = new DOMSource(xmlDoc);
        StreamResult result = new StreamResult(fileList.get(0));
        //Fix for https://github.com/apigee/apigee-deploy-maven-plugin/issues/66
        result.setSystemId(fileList.get(0).getAbsolutePath());
        transformer.transform(source, result);

    }

    public static void configureSharedFlowPackage(String env, File configFile)
            throws Exception {

        Logger logger = LoggerFactory.getLogger(PackageConfigurer.class);

        TransformerFactory transformerFactory = TransformerFactory
                .newInstance();
        Transformer transformer = transformerFactory.newTransformer();

        // get the list of files in sharedflowbundle folder
        XMLFileListUtil listFileUtil = new XMLFileListUtil();
        FileReader fileutil = new FileReader();
        ConfigTokens conf = fileutil.getBundleConfigs(configFile);
        
        
        // get the list of files in policies folder
        List<File> fileList = listFileUtil.getPolicyFiles(configFile, "sharedflowbundle");
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
                    //Fix for https://github.com/apigee/apigee-deploy-maven-plugin/issues/66
                    result.setSystemId(fileList.get(i).getAbsolutePath());
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

        // get the list of files in sharedflowbundle folder
        fileList = listFileUtil.getSharedFlowFiles(configFile);

        Document xmlDoc = fileutil.getXMLDocument(fileList.get(0)); // there would be only one file, at least one file

        javax.xml.xpath.XPathFactory factory = javax.xml.xpath.XPathFactory.newInstance();
        javax.xml.xpath.XPath xpath = factory.newXPath();
        javax.xml.xpath.XPathExpression expression = xpath.compile("/SharedFlowBundle/Description");

        Node node = (Node) expression.evaluate(xmlDoc, XPathConstants.NODE);
        if (node == null) {
            Element root = xmlDoc.getDocumentElement();
            node = xmlDoc.createElement("Description");
            root.appendChild(node);
        }

        if (node.hasChildNodes()) {
            // sets the description to whatever is in the <proxyname>.xml file
            node.setTextContent(expression.evaluate(xmlDoc));
        } else {
            // if Description is empty, then it reverts back to appending the username, git hash, etc
            node.setTextContent(getComment(fileList.get(0)));
        }

        DOMSource source = new DOMSource(xmlDoc);
        StreamResult result = new StreamResult(fileList.get(0));
        //Fix for https://github.com/apigee/apigee-deploy-maven-plugin/issues/66
        result.setSystemId(fileList.get(0).getAbsolutePath());
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

        try {
            for (int i = 0; i < configTokens.tokens.size(); i++) {

                logger.debug(
                        "=============Checking for Xpath Expressions {}  ================\n",
                        configTokens.tokens.get(i).xpath);
                javax.xml.xpath.XPathExpression expression = xpath
                        .compile(configTokens.tokens.get(i).xpath);
                
                NodeList nodes = (NodeList) expression.evaluate(doc,
                        XPathConstants.NODESET);
                for (int j = 0; j < nodes.getLength(); j++) {
                	//Fix for https://github.com/apigee/apigee-deploy-maven-plugin/issues/45
                    //if (nodes.item(j).hasChildNodes()) {
                    if (nodes.item(j).getNodeName() != null && !nodes.item(j).getNodeName().equals("")) {
                        logger.debug(
                                "=============Updated existing value {} to new value {} ================\n",
                                nodes.item(j).getTextContent(),
                                configTokens.tokens.get(i).value);
                        nodes.item(j).setTextContent(
                                configTokens.tokens.get(i).value);
                    }
                }

            }

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
            GitUtil gu = new GitUtil(basePath);
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