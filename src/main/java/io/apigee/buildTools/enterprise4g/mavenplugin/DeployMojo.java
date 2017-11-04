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



/**                                                                                                                                     ¡¡
 * Goal to upload 4g gateway  bundle on server
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

	static Logger logger = LoggerFactory.getLogger(DeployMojo.class);
	
	public DeployMojo() {
		super();

	}

	enum State {
		START, INIT, IMPORTING, DEACTIVATING, ACTIVATING, DELETING, COMPLETE
	}

	enum BUILDOPTIONS {
		NULL,deployinactive,undeploy,delete
	}
	
	enum OPTIONS {
		inactive,force,validate,clean,update,override
	}
	
	State state = State.START;
	
	String activeRevision="";
	String bundleRevision="";
	
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
					switch (OPTIONS.valueOf(opt)) {
					case validate :
                        Options.validate=true;
                        break;
					case force:
						Options.force=true;
						break;
					case inactive:
						Options.inactive=true;
						break;
					case clean:
						Options.clean=true;
						break;
					case update:
						Options.update=true;
						//set the revision that is passed as argument
						if (this.getRevision() != null) {
							revisionInArg = String.valueOf(this.getRevision());
						}
						break;
					case override:
						Options.override=true;
						break;
					default:
						break;	
					
					}
				}
			}
			
			
			
			logger.info("\n\n=============Initializing Maven Deployment================\n\n");
			
			} catch (RuntimeException e) {
				throw e;
			} catch (Exception e) {
				// This "throws Exception" bothers me so much
				throw e;
			}

	}

	

	protected void doImport() throws IOException, MojoFailureException,Exception {
		try {
			
			logger.info("\n\n=============Importing App================\n\n");
			state = State.IMPORTING;
			bundleRevision = RestUtil.uploadBundle(super.getProfile(), super.getApplicationBundlePath());

		
		} catch (IOException e) {
			throw e;
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			// This "throws Exception" bothers me so much
			throw e;
		}
	}

	
	protected void doUpdate(String revision) throws IOException, MojoFailureException,Exception {
		try {
			
			logger.info("\n\n=============Updating App================\n\n");
			state = State.IMPORTING;
			bundleRevision = RestUtil.updateBundle(super.getProfile(), super.getApplicationBundlePath(),revision);
		
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
			RestUtil.deactivateBundle(super.getProfile());
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
	
	public void doRefreshBundle()  throws IOException, MojoFailureException{
		try {
			logger.info("\n\n=============Refresh Bundle================\n\n");
			state = State.ACTIVATING;
			RestUtil.refreshBundle(super.getProfile(), this.bundleRevision);
		} catch (IOException e) {
			throw e ;
		} catch (RuntimeException e) {
			throw e;
		} 
		
	}
	
	/**
	 * Activate a bundle revision.
	 */
	
	public void doActivateBundle()  throws IOException, MojoFailureException{
		try {
			logger.info("\n\n=============Activating Bundle================\n\n");
			state = State.ACTIVATING;
			RestUtil.activateBundleRevision(super.getProfile(), this.bundleRevision);

		} catch (IOException e) {
			throw e ;
		} catch (RuntimeException e) {
			throw e;
		} 
		
	}
	
	/**
	 * Activate a bundle revision.
	 */
	
	public void doDelete(String revision) throws IOException, MojoFailureException,Exception {
		try {
			logger.info("/n/n=============Deleting App================/n/n");
			state = State.DELETING;
			RestUtil.deleteBundle(this.getProfile(), revision);

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

						if (Options.override) {
                            activeRevision=RestUtil.getDeployedRevision(this.getProfile());
                            if (activeRevision.length() > 0) {
                                doImport();
                                doActivateBundle();
                            }else {
                                Options.override=false;
                                doImport();
                                doActivateBundle();
                            }
						}
						else if (Options.update) {
							String latestRev = "";
							
							//If revision to be updated is passed in the maven command as an argument, update that revision
							if(revisionInArg.length()>0){
								logger.info("Updating Revision passed: "+ revisionInArg);
								doUpdate(revisionInArg);
								break;
							}
							
							//Check if there is a revision deployed
							activeRevision=RestUtil.getDeployedRevision(this.getProfile());
							if (activeRevision.length() > 0){
								logger.info("Active Revision: "+ activeRevision);
								logger.info("Updating Active Revision: "+ activeRevision);
								doUpdate(activeRevision);
								break;
							}else {
								doImport();
								doActivateBundle();
							}
							
							/*
							//Commenting for https://github.com/apigee/apigee-deploy-maven-plugin/issues/46
							//Check for the latest revision, if not import a new revision
						    latestRev = RestUtil.getLatestRevision(this.getProfile());
							if (latestRev.length() >0){
								logger.info("Latest Revision: "+ latestRev);
								logger.info("Updating Latest Revision: "+ latestRev);
								doUpdate(latestRev);
								break;
							}
							
							else {
								doImport();
								doActivateBundle();
							}*/
							
						}else if (Options.clean) {
                            activeRevision=RestUtil.getDeployedRevision(this.getProfile());
                                if (this.activeRevision.length() > 0) {
                                    doDelete(this.activeRevision);
                                }else {
                                  logger.info("No active revision for "+this.getProfile().getEnvironment()+"environment. Nothing to delete" );
                                }
                        }
                        else {
							doImport();
							if (!Options.inactive)
								doRefreshBundle();
                        }

						break;
				case deployinactive:
                        logger.warn("Note: -Dbuild.option=deploy-inactive   is Deprecated, use -Dapigee.options=inactive instead");
						doImport();
						break;
				case undeploy:
                        logger.warn("Note: -Dbuild.option=undeploy is Deprecated, use -Dapigee.options=clean instead");
						 doDeactivae();
	                     break;
				
				case delete:
						activeRevision=RestUtil.getDeployedRevision(this.getProfile());
						doDelete(activeRevision);
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




