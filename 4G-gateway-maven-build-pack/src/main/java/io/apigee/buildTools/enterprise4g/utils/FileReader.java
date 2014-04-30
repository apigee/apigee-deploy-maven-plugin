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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.google.gson.Gson;

/**
 * Goal is to read files and return appropriate java objects, dom for XML
 * 
 * @author sdey
 */

public class FileReader {

	public Document getXMLDocument(File filepath) throws SAXException,
			IOException, ParserConfigurationException

	{
		// Create a builder factory
		DocumentBuilderFactory docFactory = DocumentBuilderFactory
				.newInstance();

		// Create the builder and parse the file
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		//Document doc = docBuilder.parse("/Users/santanudey/Projects/4G/code-fest/project/apiproxy/proxies/proxy.xml");
		Document doc = docBuilder.parse(filepath);
		return doc;
	}

	public ConfigTokens getBundleConfigs(File filepath) throws FileNotFoundException

	{
		ConfigTokens conf = new ConfigTokens();

		Gson gson = new Gson();

		BufferedReader bufferedReader = new BufferedReader(
				new java.io.FileReader(filepath));
		conf = gson.fromJson(bufferedReader, ConfigTokens.class); // binds the file to data config objects
		
		Logger logger = LoggerFactory.getLogger(FileReader.class);
		
//		logger.info("\n\n=============Reading the config file================\n\n{}", conf.toString());
		logger.info("============= Reading the config file located at ================\n{}",  filepath.getAbsolutePath());
		
		
		
		return conf;
	}

}
