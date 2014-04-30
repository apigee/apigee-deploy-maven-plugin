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

import java.io.File;

import io.apigee.buildTools.enterprise4g.utils.ServerProfile;
import org.apache.maven.plugin.AbstractMojo;



public abstract class GatewayAbstractMojo extends AbstractMojo {

	/**
	 * Directory containing the build files.
	 * 
	 * @parameter expression="${project.build.directory}"
	 */
	private File buildDirectory;
	
	/**
	 * Base directory of the project.
	 * 
	 * @parameter expression="${basedir}"
	 */
	private File baseDirectory;

	/**
	 * Project Name
	 * 
	 * @parameter expression="${project.name}"
	 */
	private String projectName;
	
	/**
	 * Project version
	 * 
	 * @parameter expression="${project.version}"
	 */
	private String projectVersion;

	/**
	 * Project artifact id
	 * 
	 * @parameter expression="${project.artifactId}"
	 */
	private String artifactId;
	
	/**
	 * Profile id
	 * 
	 * @parameter expression="${apigee.profile}"
	 */
	private String id;
	

	/**
	 * Gateway host URL
	 * 
	 * @parameter expression="${apigee.hosturl}"
	 */
	private String hostURL;
	

	/**
	 * Gateway env profile
	 * 
	 * @parameter expression="${apigee.env}" default-value="${apigee.profile}"
	 */
	private String deploymentEnv;
	
	/**
	 * Gateway api version
	 * 
	 * @parameter expression="${apigee.apiversion}"
	 */
	private String apiVersion;
	
	
	/**
	 * Gateway org name
	 * 
	 * @parameter expression="${apigee.org}"
	 */
	private String orgName;
	
	/**
	 * Gateway host username
	 * 
	 * @parameter expression="${apigee.username}"
	 */
	private String userName;
	
	/**
	 * Gateway host password
	 * 
	 * @parameter expression="${apigee.password}"
	 */
	private String password;

	/**
	 * Build option
	 * 
	 * @parameter expression="${build.option}"
	 */
	private String buildOption;
	
	
	/**
	 * Gateway options
	 * 
	 * @parameter expression="${apigee.options}"
	 */
	private String options;
	
	
	/**
	 * Gateway delay
	 * 
	 * @parameter expression="${apigee.delay}"
	 */
	private Long delay;
	
	
	/**
	 * Gateway delay
	 * 
	 * @parameter expression="${apigee.override.delay}"
	 */
	private Long overridedelay;
	
	
	/**
	* Skip running this plugin.
	* Default is false.
	*
	* @parameter default-value="false"
	*/
	private boolean skip = false;

	public ServerProfile buildProfile;

	public GatewayAbstractMojo(){
		super();
		
	}
	
	
	public ServerProfile getProfile() {
		this.buildProfile = new ServerProfile();
		this.buildProfile.setOrg(this.orgName);
		this.buildProfile.setApplication(this.projectName);
		this.buildProfile.setApi_version(this.apiVersion);
		this.buildProfile.setHostUrl(this.hostURL);
		this.buildProfile.setEnvironment(this.deploymentEnv);
		this.buildProfile.setCredential_user(this.userName);
		this.buildProfile.setCredential_pwd(this.password);
		this.buildProfile.setProfileId(this.id);
		this.buildProfile.setOptions(this.options);
		this.buildProfile.setDelay(this.delay);
		this.buildProfile.setOverridedelay(this.overridedelay);
		
		return buildProfile;
	}

	public void setProfile(ServerProfile profile) {
		this.buildProfile = profile;
	}

	public void setBaseDirectory(File baseDirectory) {
		this.baseDirectory = baseDirectory;
	}
	public String getBuildDirectory() {
		return this.buildDirectory.getAbsolutePath(); 
	}

	public String getApplicationBundlePath() {
		return this.baseDirectory+File.separator+"target"+File.separator+this.projectName+this.projectVersion+"-"+this.id+".zip";
	}
	
	public String getBaseDirectoryPath(){
		return this.baseDirectory.getAbsolutePath();
	}


	public String getBuildOption() {
		return buildOption;
	}


	public void setBuildOption(String buildOption) {
		this.buildOption = buildOption;
	}

	public String getOptions() {
		return options;
	}


	public void setOptions(String options) {
		this.options = options;
	}
	

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}


	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}


	public boolean isSkip() {
		return skip;
	}


	public void setSkip(boolean skip) {
		this.skip = skip;
	}
	

	protected void fixOSXNonProxyHosts() {
		
		// This is required to work around a Mac OS X bug.  Probably would be better to be more selective about this.  Only override if we're
		// actually on OS X.  Or look at the value and only unset if it contains the problematic wildcard value.
		try {
			System.setProperty("http.nonProxyHosts", "");
		}
		catch (RuntimeException e) {
			// just try to continue
			getLog().error(e);
		}
	}

	public Long getDelay() {
		return this.delay;
	}


	public void setDelay(Long delay) {
		this.delay = delay;
	}

	public Long getOverridedelay() {
		return this.overridedelay;
	}


	public void setOverridedelay(Long overridedelay) {
		this.overridedelay = overridedelay;
	}
	
	
}
