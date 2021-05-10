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
package io.apigee.buildTools.enterprise4g.mavenplugin;

import io.apigee.buildTools.enterprise4g.utils.PackageConfigurer;
import io.apigee.buildTools.enterprise4g.utils.ZipUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.io.FileUtils;

import java.io.File;


/**
 * Goal to upload 4g gateway  bundle on server
 *
 * @author sdey
 * @goal configure
 * @phase package
 */

public class ConfigureMojo extends GatewayAbstractMojo {


	public void execute() throws MojoExecutionException, MojoFailureException {

		if (super.isSkip()) {
			getLog().info("Skipping");
			return;
		}

		Logger logger = LoggerFactory.getLogger(ConfigureMojo.class);
		File configFile = findConfigFile(logger);

		if (configFile != null) {
			configurePackage(logger, configFile);
		}

        logger.info("\n\n=============Checking for node.js app================\n\n");
        //sometimes node.js source lives outside the apiproxy/ in node/ within the base project directory
        String externalNodeDirPath = super.getBaseDirectoryPath() + "/node/";
        File externalNodeDir = new File(externalNodeDirPath);

        //if node.js source is inside apiproxy/ directory, it will be in the build directory
        String nodeDirPath = super.getBuildDirectory() + "/apiproxy/resources/node/";
        File nodeDir = new File(nodeDirPath);


        //if we find files in the node/ directory outside apiproxy/, move into apiproxy/resources/node
        //this takes precedence and we will overwrite potentially stale node.js code in  apiproxy/resources/node
        if(externalNodeDir.isDirectory()){
            String[] filesInExternalNodeDir = externalNodeDir.list();
            if(filesInExternalNodeDir.length > 0) {
                logger.info("Node.js app code found outside apiproxy/ directory. Moving to target/apiproxy/resources/node (will overwrite).");
                try {
                    FileUtils.deleteDirectory(nodeDir);
                    FileUtils.copyDirectory(externalNodeDir, nodeDir);
                }catch(Exception e){
                    throw new MojoExecutionException(e.getMessage());
                }
            }
        }

        //always handle zipping of any directories in apiproxy/resources/node
		if (nodeDir.isDirectory()) {
            logger.info("\n\n=============Now zipping node modules================\n\n");

            String[] filesInNodeDir = nodeDir.list();

            for (String fileName : filesInNodeDir) {
                String filePath = nodeDirPath + fileName;
                File dirFile = new File(filePath);
                if (dirFile.isDirectory() && fileName.contains("node_modules"))
                {
                    logger.info("Zipping " + fileName + " (it is a directory).");
                    try {
                        ZipUtils zu = new ZipUtils();
                        zu.zipDir(new File(filePath + ".zip"),
                                dirFile, fileName);
                        FileUtils.deleteDirectory(dirFile);
                    } catch (Exception e) {
                        throw new MojoExecutionException(e.getMessage());
                    }
                }
            }

        }

		logger.info("\n\n=============Now zipping the App Bundle================\n\n");

		//Zip package
		zipDirectory();
	}
	
	private void zipDirectory() throws MojoExecutionException{
		try {
			ZipUtils zu = new ZipUtils();
			if(super.getProfile().getApi_type() != null && super.getProfile().getApi_type().equalsIgnoreCase("sharedflow")){
				zu.zipDir(new File(super.getApplicationBundlePath()),
						new File(super.getBuildDirectory() + "/sharedflowbundle"), "sharedflowbundle");
			}
			else{
				zu.zipDir(new File(super.getApplicationBundlePath()),
						new File(super.getBuildDirectory() + "/apiproxy"), "apiproxy");
			}
		} catch (Exception e) {
			throw new MojoExecutionException(e.getMessage());
		}
	}

	private void configurePackage(Logger logger, File configFile) throws MojoExecutionException {
		logger.debug("\n\n=============Now updating the configuration values for the App Bundle================\n\n");
		try {
			if (super.getProfile().getProfileId() != null && super.getProfile().getProfileId() != ""
					&& super.getProfile().getApi_type() != null && super.getProfile().getApi_type().equalsIgnoreCase("sharedflow")) {
				PackageConfigurer.configureSharedFlowPackage(super.getProfile().getProfileId(), configFile);
			}else if (super.getProfile().getProfileId() != null && super.getProfile().getProfileId() != "") {
				PackageConfigurer.configurePackage(super.getProfile().getProfileId(), configFile);
			} else {
				PackageConfigurer.configurePackage(super.getProfile().getEnvironment(), configFile);
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
			throw new MojoExecutionException(e.getMessage());
		}
	}

	private File findConfigFile(Logger logger) throws MojoExecutionException {
		File configFile = new File(super.getBaseDirectoryPath() + File.separator + "config.json");

		if (configFile.exists()) {
			return configFile;
		}

		logger.info("No config.json found. Skipping package configuration.");
		return null;
	}
}
