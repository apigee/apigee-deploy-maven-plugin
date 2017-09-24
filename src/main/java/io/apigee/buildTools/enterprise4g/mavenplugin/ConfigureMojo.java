/**
 * Copyright (C) 2014 Apigee Corporation
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.apigee.buildTools.enterprise4g.mavenplugin;

import io.apigee.buildTools.enterprise4g.rest.Bundle;
import io.apigee.buildTools.enterprise4g.utils.PackageConfigurer;
import io.apigee.buildTools.enterprise4g.utils.ServerProfile;
import io.apigee.buildTools.enterprise4g.utils.ZipUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
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

		if (isSkip()) {
			getLog().info("Skipping");
			return;
		}

		File configFile = findConfigFile();

		// Need a server profile but can skip auth validation as we dont do anything with it.
		ServerProfile profile = createProfile(false);

		Bundle bundle = createBundle();

		if (configFile != null) {
			configurePackage(profile, configFile, bundle);
		}

		getLog().info("Checking for node.js app");
		//sometimes node.js source lives outside the apiproxy/ in node/ within the base project directory
		String externalNodeDirPath = getBaseDirectoryPath() + "/node/";
		File externalNodeDir = new File(externalNodeDirPath);

		//if node.js source is inside apiproxy/ directory, it will be in the build directory
		String nodeDirPath = getBuildDirectory() + "/apiproxy/resources/node/";
		File nodeDir = new File(nodeDirPath);


		//if we find files in the node/ directory outside apiproxy/, move into apiproxy/resources/node
		//this takes precedence and we will overwrite potentially stale node.js code in  apiproxy/resources/node
		if (externalNodeDir.isDirectory()) {
			String[] filesInExternalNodeDir = externalNodeDir.list();
			if (filesInExternalNodeDir.length > 0) {
				getLog().info("Node.js app code found outside apiproxy/ directory. Moving to target/apiproxy/resources/node (will overwrite).");
				try {
					FileUtils.deleteDirectory(nodeDir);
					FileUtils.copyDirectory(externalNodeDir, nodeDir);
				} catch (Exception e) {
					throw new MojoExecutionException(e.getMessage());
				}
			}
		}

		//always handle zipping of any directories in apiproxy/resources/node
		if (nodeDir.isDirectory()) {
			getLog().info("Now zipping node modules");
			String[] filesInNodeDir = nodeDir.list();
			for (String fileName : filesInNodeDir) {
				String filePath = nodeDirPath + fileName;
				File dirFile = new File(filePath);
				if (dirFile.isDirectory() && fileName.contains("node_modules")) {
					getLog().info("Zipping " + fileName + " (it is a directory).");
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
		zipDirectory(profile, bundle);
	}

	private void zipDirectory(ServerProfile profile, Bundle bundle) throws MojoExecutionException {
		try {
			File bundleFile = new File(getApplicationBundlePath());
			getLog().info("Create Apigee App Bundle " + bundleFile.getAbsolutePath());
			ZipUtils zu = new ZipUtils();

			if (Bundle.Type.SHAREDFLOW == bundle.getType()) {
				zu.zipDir(bundleFile, new File(getBuildDirectory(), "sharedflowbundle"), "sharedflowbundle");
			} else {
				zu.zipDir(bundleFile, new File(getBuildDirectory(), "apiproxy"), "apiproxy");
			}
		} catch (Exception e) {
			throw new MojoExecutionException(e.getMessage(), e);
		}
	}

	private void configurePackage(ServerProfile profile, File configFile, Bundle bundle) throws MojoExecutionException {
		getLog().debug("Updating the configuration values for the App Bundle");
		try {
			if (profile.getProfileId() != null && profile.getProfileId() != "" &&
					Bundle.Type.SHAREDFLOW == bundle.getType()) {
				PackageConfigurer.configureSharedFlowPackage(profile.getProfileId(), configFile);
			} else if (profile.getProfileId() != null && profile.getProfileId() != "") {
				PackageConfigurer.configurePackage(profile.getProfileId(), configFile);
			} else {
				PackageConfigurer.configurePackage(profile.getEnvironment(), configFile);
			}
		} catch (Exception e) {
			throw new MojoExecutionException(e.getMessage(), e);
		}
	}

	private File findConfigFile() throws MojoExecutionException {
		File configFile = new File(getBaseDirectoryPath(), "config.json");

		if (configFile.exists()) {
			return configFile;
		}

		getLog().info("No config.json found. Skipping package configuration.");
		return null;
	}

}
