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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//import javax.xml.parsers.ParserConfigurationException;
//import javax.xml.transform.SourceLocator;
//import javax.xml.xpath.XPathExpressionException;
//
//import org.apache.xpath.XPath;
//import org.w3c.dom.Document;
//import org.xml.sax.SAXException;

public class XMLFileListUtil {

	//	public static void main(String[] args) throws IOException, SAXException, ParserConfigurationException, XPathExpressionException {
	//		XMLFileListUtil util = new XMLFileListUtil();
	//
	//		File folder1 = new File(
	//				"/Users/santanudey/Projects/4G/code-fest/project/apiproxy/proxies");
	//		List<File> fileList = util.getXMLFiles(folder1);
	//		FileReader fr= new FileReader();
	//		int i = 0;
	//		while (i < fileList.size()) {
	//			
	//			Document dom = fr.getXMLDocument(fileList.get(i));
	//			
	//			javax.xml.xpath.XPathFactory factory = 
	//            javax.xml.xpath.XPathFactory.newInstance();
	//			javax.xml.xpath.XPath xpath = factory.newXPath();
	//			javax.xml.xpath.XPathExpression expression = xpath.compile("/ProxyEndpoint/Description/text()");
	//
	//			System.out.println(((expression.evaluate(dom))));
	//			i++;
	//		}
	//
	//	}

	public List<File> getProxyFiles(File configFile) throws IOException { // assumes the present directory is at
		// the project pom level.

		List<File> fileNames = new ArrayList<File>();
		String sDirectory = configFile.getParent()+File.separator+"target" + File.separator + "apiproxy" + File.separator + "proxies";

		fileNames = getXMLFiles(sDirectory);

		return fileNames;

	}

	public List<File> getPolicyFiles(File configFile) throws IOException {
		return getPolicyFiles(configFile, "apiproxy");
	}
	
	public List<File> getPolicyFiles(File configFile, String dirName) throws IOException { // assumes the present directory is at
		// the project pom level.

		List<File> fileNames = new ArrayList<File>();

		String sDirectory = configFile.getParent()+File.separator+"target" + File.separator + dirName + File.separator + "policies";

		fileNames = getXMLFiles(sDirectory);

		List<File> stepNames = getStepFiles(configFile);
		if(stepNames != null) {
			fileNames.addAll(stepNames);
		}

		return fileNames;

	}

	private List<File> getStepFiles(File configFile) throws IOException { // assumes the present directory is at
		// the project pom level.

		List<File> fileNames = new ArrayList<File>();

		String sDirectory = configFile.getParent()+File.separator+"target" + File.separator + "apiproxy" + File.separator + "stepdefinitions";

		fileNames = getXMLFiles(sDirectory);

		return fileNames;

	}

	public List<File> getTargetFiles(File configFile) throws IOException { // assumes the present directory is at
		// the project pom level.

		List<File> fileNames = new ArrayList<File>();

		String sDirectory = configFile.getParent()+File.separator+"target" + File.separator + "apiproxy" + File.separator + "targets";

		fileNames = getXMLFiles(sDirectory);

		return fileNames;

	}
	
	public List<File> getAPIProxyFiles(File configFile) throws IOException { // assumes the present directory is at
		// the project pom level.

		List<File> fileNames = new ArrayList<File>();

		String sDirectory = configFile.getParent()+File.separator+"target" + File.separator + "apiproxy";

		fileNames = getXMLFiles(sDirectory);

		return fileNames;

	}
	
	public List<File> getSharedFlowFiles(File configFile) throws IOException { // assumes the present directory is at
		// the project pom level.

		List<File> fileNames = new ArrayList<File>();

		String sDirectory = configFile.getParent()+File.separator+"target" + File.separator + "sharedflowbundle";

		fileNames = getXMLFiles(sDirectory);

		return fileNames;

	}

	private List<File> getXMLFiles(String sFolder) { // assumes the present
		// directory is at the
		// project pom level.

		ArrayList<File> aList = new ArrayList<File>();
		Logger logger = LoggerFactory.getLogger(XMLFileListUtil.class);
		try {
			File folder = new File(sFolder);

			ExtFileNameFilter xmlFilter = new ExtFileNameFilter("xml");

			logger.debug("=============Searching for XML files in the following directory ================\n{}", folder.getAbsolutePath());

			aList = new ArrayList<File>(Arrays.asList(folder.listFiles(xmlFilter)));

			logger.debug("=============Number of files found is================\n{}", aList.size());
		} catch (Exception e) {
			logger.debug("=============Error Encountered in Searching files [" + sFolder + "]================\n" + e);
		}

		return aList;

	}

}
