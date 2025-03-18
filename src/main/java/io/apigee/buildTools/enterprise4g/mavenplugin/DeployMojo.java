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

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import io.apigee.buildTools.enterprise4g.rest.RestUtil;
import io.apigee.buildTools.enterprise4g.rest.RestUtil.Options;



/**                                                                                                                                     ¡¡
 * Goal to deploy to Apigee
 * @author rmishra
 * @goal deploy
 * @phase package
 * 
 */

public class DeployMojo extends GatewayAbstractMojo
{

	
	
	public static final String DEPLOYMENT_FAILED_MESSAGE = "\n\n\n* * * * * * * * * * *\n\n"
			+ "This deployment could have failed for a variety of reasons.\n\n"
			+ "\n\n* * * * * * * * * * *\n\n\n";

	static Logger logger = LogManager.getLogger(DeployMojo.class);
	
	public DeployMojo() {
		super();

	}

	enum State {
		START, INIT, IMPORTING, DEACTIVATING, ACTIVATING, DELETING, COMPLETE
	}

	enum BUILDOPTIONS {
		NULL,clean,inactive
	}
	
	enum OPTIONS {
		override,async,clean,inactive
	}
	
	State state = State.START;
	
//	String activeRevision="";
//	String bundleRevision="";
	
	//revision passed in the maven argument
	String revisionInArg = "";
	
	BUILDOPTIONS buildOption;
	


	public void init() throws IOException, MojoFailureException,Exception {
		try {

			String options="";
			state = State.INIT;
			long delay=0;
			
			if (this.getBuildOption() != null) {
				String opt = this.getBuildOption();
				//To Support legacy 
				opt = opt.replace("-", "");
				buildOption=BUILDOPTIONS.valueOf(opt);
			}
			else {
				buildOption=BUILDOPTIONS.valueOf("NULL");
			}
			
			//Options.delay=0;
			if (this.getDelay() != null) {
				delay = this.getDelay();
				Options.delay=delay;
			}
			if (this.getOverridedelay() != null) {
				delay = this.getOverridedelay();
				Options.override_delay=delay;
			}
			
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
					case clean:
						Options.clean=true;
						buildOption=BUILDOPTIONS.valueOf("clean");
						break;
					case inactive:
						Options.inactive=true;
						buildOption=BUILDOPTIONS.valueOf("inactive");
						break;
					default:
						break;
					}
				}
			}else
				Options.override=true;
			
			
			
			logger.info("\n\n=============Initializing Maven Deployment================\n\n");
			
			} catch (RuntimeException e) {
				throw e;
			} catch (Exception e) {
				// This "throws Exception" bothers me so much
				throw e;
			}

	}

	

	protected String doImport() throws IOException, MojoFailureException,Exception {
		String bundleRevision;
		try {
			
			logger.info("\n\n=============Importing App================\n\n");
			state = State.IMPORTING;
			RestUtil restUtil = new RestUtil(super.getProfile());
			bundleRevision = restUtil.uploadBundle(super.getProfile(), super.getApplicationBundlePath());

		
		} catch (IOException e) {
			throw e;
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			// This "throws Exception" bothers me so much
			throw e;
		}
		return bundleRevision;
	}

	
	protected void doUpdate(String revision) throws IOException, MojoFailureException,Exception {
		try {
			
			logger.info("\n\n=============Updating App================\n\n");
			state = State.IMPORTING;
			RestUtil restUtil = new RestUtil(super.getProfile());
			String bundleRevision = restUtil.updateBundle(super.getProfile(), super.getApplicationBundlePath(),revision);
		
		} catch (IOException e) {
			throw e;
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			// This "throws Exception" bothers me so much
			throw e;
		}
	}
	
	/**
	 * Deactivae a bundle revision.
	 */
	
	public void doDeactivae() throws IOException, MojoFailureException,Exception {
		try {
			logger.info("\n\n=============Deactivating App================\n\n");
			state = State.DEACTIVATING;
			RestUtil restUtil = new RestUtil(super.getProfile());
			restUtil.deactivateBundle(super.getProfile());
		}
		catch (IOException e) {
				throw e ;	
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw e;
		}
	}
	
	/**
	 * Refresh a bundle revision.
	 */
	
	public void doRefreshBundle(String bundleRevision)  throws IOException, MojoFailureException{
		try {
			logger.info("\n\n=============Refresh Bundle================\n\n");
			state = State.ACTIVATING;
			RestUtil restUtil = new RestUtil(super.getProfile());
			restUtil.refreshBundle(super.getProfile(), bundleRevision);
		} catch (IOException e) {
			throw e ;
		} catch (RuntimeException e) {
			throw e;
		} 
		
	}
	
	/**
	 * Activate a bundle revision.
	 * @throws InterruptedException 
	 */
	
	public void doActivateBundle(String bundleRevision)  throws IOException, MojoFailureException, InterruptedException{
		try {
			logger.info("\n\n=============Activating Bundle================\n\n");
			state = State.ACTIVATING;
			RestUtil restUtil = new RestUtil(super.getProfile());
			String revision = restUtil.activateBundleRevision(super.getProfile(), bundleRevision);

			// if user passed -Dapigee.options=async, no need for polling, exit early
			if (Options.async) {
				return;
			}
			Thread.sleep(10*1000); //Introducing this delay to avoid the 503 error while performing GET immediately after the POST deployments
			boolean deployed = false;
			//Loop to check the deployment status
			for (; !deployed; ) {
				deployed = restUtil.getDeploymentStateForRevision(super.getProfile(), revision);
	        	Thread.sleep(10*1000);
			}
		} catch (IOException e) {
			throw e ;
		} catch (RuntimeException e) {
			throw e;
		} 
		Thread.sleep(5*1000);
	}
	
	/**
	 * Delete a bundle.
	 */
	
	public void doDelete() throws IOException, MojoFailureException,Exception {
	try {
			RestUtil restUtil = new RestUtil(this.getProfile());
			String status = restUtil.deactivateBundle(this.getProfile());
			if(status == null) {
				logger.info("No bundle to delete");
				return;
			}
			logger.info("\n\n=============Deleting bundle================\n\n");
			state = State.DELETING;
			restUtil.deleteBundle(this.getProfile());
		} catch (IOException e) {
			throw e ;
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw e;
		}
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
			
			switch (buildOption) {
				case NULL:
						String rev = doImport();
						doActivateBundle(rev);
						break;
				case clean:
						doDelete();
						break;
				case inactive:
						doImport();
						break;
				default:     
						break;
			}
		
			
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

	private void processHelpfulErrorMessage(Exception e)
			throws MojoExecutionException {
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




