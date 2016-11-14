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

import io.apigee.buildTools.enterprise4g.rest.RestUtil;
import io.apigee.buildTools.enterprise4g.rest.RestUtil.Options;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;



/**
 * Goal to initialise multifactor authentication / oauth token
 * @author dpickard
 * @execute phase="validate"
 * @goal initmfa
 * @phase validate
 * 
 */

public class InitMfaMojo extends GatewayAbstractMojo
{
	
	public InitMfaMojo() {
		super();

	}
	
	/** 
	 * Entry point for the mojo.
	 */
	public void execute() throws MojoExecutionException, MojoFailureException {

		try {
			RestUtil.initMfa(this.getProfile());
		} catch (RuntimeException e) {
			throw new MojoExecutionException("", e);
		} catch (Exception e) {
			throw new MojoExecutionException("", e);
		} finally {
			
		}
	}

}
