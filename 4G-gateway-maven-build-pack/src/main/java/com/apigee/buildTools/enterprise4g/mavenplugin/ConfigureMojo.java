package com.apigee.buildTools.enterprise4g.mavenplugin;

import com.apigee.buildTools.enterprise4g.utils.PackageConfigurer;
import com.apigee.cs.util.ZipUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.IOException;


/**
 * Goal to upload 4g gateway  bundle on server
 * @author sdey
 * @goal configure
 * 
 * @phase package
 * 
 */

public class ConfigureMojo  extends GatewayAbstractMojo{
	
	

	public void execute() throws MojoExecutionException, MojoFailureException {

		if (super.isSkip()) {
			getLog().info("Skipping");
			return;
			}
		
		File configFile;
		Logger logger = LoggerFactory.getLogger(ConfigureMojo.class);
		
		try {
			configFile = new File(super.getBaseDirectoryPath()+File.separator+"config.json");
		} catch (NullPointerException e) {
			// TODO Auto-generated catch block
			logger.debug("\n\n=============Could not find the config.json file================\n\n");
			logger.error(e.getMessage());
			throw new MojoExecutionException(e.getMessage());
		}
		
		logger.debug("\n\n=============Now updating the configuration values for the App Bundle================\n\n");
		try {
			if(super.getProfile().getProfileId() != null &&super.getProfile().getProfileId()!="") {
				PackageConfigurer.configurePackage(super.getProfile().getProfileId(), configFile);	
			}
			else {
				PackageConfigurer.configurePackage(super.getProfile().getEnvironment(), configFile);
			}

		} catch (XPathExpressionException e) {
			logger.error(e.getMessage());
			throw new MojoExecutionException(e.getMessage());
		} catch (SAXException e) {
			logger.error(e.getMessage());
			throw new MojoExecutionException(e.getMessage());
		} catch (IOException e) {
			logger.error(e.getMessage());
			throw new MojoExecutionException(e.getMessage());
		} catch (ParserConfigurationException e) {
			logger.error(e.getMessage());
			throw new MojoExecutionException(e.getMessage());
		} catch (TransformerException e) {
			logger.error(e.getMessage());
			throw new MojoExecutionException(e.getMessage());
		}catch (Exception e) {
			logger.error(e.getMessage());
			throw new MojoExecutionException(e.getMessage());
		}

		logger.info("\n\n=============Now zipping the App Bundle================\n\n");

		try {
			//ZipFolder.ZipFolder(super.getBuildDirectory(), super.getApplicationBundlePath());
			ZipUtils zu = new ZipUtils();
			zu.zipDir(new File (super.getApplicationBundlePath()), new File(super.getBuildDirectory()+"/apiproxy"), "apiproxy");
		}catch(Exception e)
		{
			throw new MojoExecutionException(e.getMessage());
		}
	}
}
