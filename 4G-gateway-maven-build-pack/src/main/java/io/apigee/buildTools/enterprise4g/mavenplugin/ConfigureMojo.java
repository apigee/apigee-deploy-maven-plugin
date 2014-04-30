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

        logger.info("\n\n=============Now zipping the App Bundle================\n\n");

        try {
            ZipUtils zu = new ZipUtils();
            zu.zipDir(new File(super.getApplicationBundlePath()),
                    new File(super.getBuildDirectory() + "/apiproxy"), "apiproxy");
        } catch (Exception e) {
            throw new MojoExecutionException(e.getMessage());
        }
    }

    private void configurePackage(Logger logger, File configFile) throws MojoExecutionException {
        logger.debug("\n\n=============Now updating the configuration values for the App Bundle================\n\n");
        try {
            if (super.getProfile().getProfileId() != null && super.getProfile().getProfileId() != "") {
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