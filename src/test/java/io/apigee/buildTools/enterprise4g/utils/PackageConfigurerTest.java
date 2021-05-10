package io.apigee.buildTools.enterprise4g.utils;

import junit.framework.TestCase;
import org.w3c.dom.Document;

import java.io.*;

public class PackageConfigurerTest extends TestCase {

    private FileReader fileReader = new FileReader();

    public void testConfigurePackage() throws Exception {
        File configFile = new File(getClass().getClassLoader().getResource("configTest/config.json").toURI());
        PackageConfigurer.configurePackage("test", configFile);

        File policyFile = new File(getClass().getClassLoader().getResource("configTest/target/apiproxy/policies/QuotaPolicy.xml").toURI());
        Document xmlDocument = fileReader.getXMLDocument(policyFile);

        javax.xml.xpath.XPathFactory factory = javax.xml.xpath.XPathFactory.newInstance();
        javax.xml.xpath.XPath xpath = factory.newXPath();
        assertEquals("2", xpath.evaluate("/Quota/Interval", xmlDocument));
    }
}