/**
 * Copyright 2023 Google Inc.
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

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import io.apigee.buildTools.enterprise4g.mavenplugin.DeployMojo.BUILDOPTIONS;
import io.apigee.buildTools.enterprise4g.mavenplugin.DeployMojo.OPTIONS;
import io.apigee.buildTools.enterprise4g.rest.RestUtil;
import io.apigee.buildTools.enterprise4g.rest.RestUtil.Options;
import io.apigee.buildTools.enterprise4g.utils.ServerProfile;

/**
 * ¡¡ Goal to promote a revision to other environments
 * 
 * @author ssvaidyanathan
 * @goal promote
 * @phase install
 * 
 */

public class PromoteMojo extends GatewayAbstractMojo {

	public static final String DEPLOYMENT_FAILED_MESSAGE = "\n\n\n* * * * * * * * * * *\n\n"
			+ "This deployment could have failed for a variety of reasons.\n\n" + "\n\n* * * * * * * * * * *\n\n\n";

	static Logger logger = LogManager.getLogger(PromoteMojo.class);

	public PromoteMojo() {
		super();

	}

	enum State {
		START, INIT, IMPORTING, DEACTIVATING, ACTIVATING, DELETING, COMPLETE
	}

	enum BUILDOPTIONS {
		NULL, clean
	}

	enum OPTIONS {
		override, async, clean
	}

	State state = State.START;

	String activeRevision = "";
	String bundleRevision = "";

	// revision passed in the maven argument
	String revisionInArg = "";

	BUILDOPTIONS buildOption;

	public void init() throws IOException, MojoFailureException, Exception {
		try {
			String options="";
			state = State.INIT;
			
			options=super.getOptions();
			if (options != null) {
				String [] opts = options.split(",");
				for (String opt : opts) {
					opt = opt.replace("-", "");
					switch (OPTIONS.valueOf(opt)) {
					case override:
						Options.override=true;
						break;
					case async:
						Options.override=true;
						Options.async=true;
						break;
					default:
						break;
					}
				}
			}else
				Options.override=true;

			if (super.getProfile().getPromoteSourceEnv() != null
					&& !super.getProfile().getPromoteSourceEnv().equals("")) {
				doPromoteFromSourceEnv(super.getProfile().getPromoteSourceEnv());
			} else if (super.getProfile().getPromoteRevision() != null
					&& !super.getProfile().getPromoteRevision().equals("")) {
				doPromoteRevision(super.getProfile().getPromoteRevision());
			} else {
				doPromoteLatestRevision();
			}

		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			// This "throws Exception" bothers me so much
			throw e;
		}

	}

	protected void doPromoteFromSourceEnv(String sourceEnv) throws IOException, MojoFailureException, Exception {
		// Check if sourceEnv exist in the org
		// Get the deployed revision from the sourceEnv and deploy that revision to the target env
		// If there are no revisions deployed to the sourceEnv, then throw an error
		try {
			RestUtil restUtil = new RestUtil(super.getProfile());
			String deployedRevision = restUtil.getDeployedRevision(super.getProfile(), sourceEnv);
			if(deployedRevision!=null && !deployedRevision.equals(""))
				doPromoteRevision(deployedRevision);
			else
				throw new RuntimeException("No revisions deployed in the "+sourceEnv+" environment");
		}
		catch (IOException e) {
			throw e;
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw e;
		}
	}

	protected void doPromoteRevision(String promoteRevision) throws IOException, MojoFailureException, Exception {
		// Get the revision and deploy to target environment
		// If the revision number is incorrect, then throw an error
		try {
			bundleRevision = promoteRevision;
			doActivateBundle();
		}
		catch (IOException e) {
			throw e;
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw e;
		}
	}

	protected void doPromoteLatestRevision() throws IOException, MojoFailureException, Exception {
		// Fetch the latest revision for the proxy in the org and deploy that to the target env
		// If there are no revisions, then throw an error
		try {
			RestUtil restUtil = new RestUtil(super.getProfile());
			bundleRevision = restUtil.getLatestRevision(super.getProfile());
			doActivateBundle();
		}
		catch (IOException e) {
			throw e;
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * Activate a bundle revision.
	 * 
	 * @throws InterruptedException
	 */

	public void doActivateBundle() throws IOException, MojoFailureException, InterruptedException {
		try {
			logger.info("\n\n=============Activating Bundle================\n\n");
			state = State.ACTIVATING;
			RestUtil restUtil = new RestUtil(super.getProfile());
			String revision = restUtil.activateBundleRevision(super.getProfile(), this.bundleRevision);

			// if user passed -Dapigee.options=async, no need for polling, exit early
			if (Options.async) {
				return;
			}

			boolean deployed = false;
			// Loop to check the deployment status
			for (; !deployed;) {
				deployed = restUtil.getDeploymentStateForRevision(super.getProfile(), revision);
				Thread.sleep(5 * 1000);
			}
		} catch (IOException e) {
			throw e;
		} catch (RuntimeException e) {
			throw e;
		}
		Thread.sleep(5 * 1000);
	}


	/**
	 * Entry point for the mojo.
	 */
	public void execute() throws MojoExecutionException, MojoFailureException {
		if (super.isSkip()) {
			getLog().info("Skipping");
			return;
		}
		try {
			init();
			state = State.COMPLETE;

		} catch (MojoFailureException e) {
			processHelpfulErrorMessage(e);
		} catch (RuntimeException e) {
			processHelpfulErrorMessage(e);
		} catch (Exception e) {
			processHelpfulErrorMessage(e);
		} finally {

		}
	}

	private void processHelpfulErrorMessage(Exception e) throws MojoExecutionException {
		if (state == State.IMPORTING) {
			logger.error(DEPLOYMENT_FAILED_MESSAGE);
		}

		if (e instanceof MojoExecutionException) {
			throw (MojoExecutionException) e;
		} else {
			throw new MojoExecutionException("", e);
		}

	}

}
