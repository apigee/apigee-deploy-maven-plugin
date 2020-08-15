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
package io.apigee.buildTools.enterprise4g.utils;

import io.apigee.buildTools.enterprise4g.rest.ActionFlags;
import org.apache.http.client.HttpClient;

import java.util.EnumSet;

public class ServerProfile {

	// general configuration
	private String hostURL; // hostname & scheme e.g. https://api.enterprise.apigee.com
	private String org;
	private String environment; // prod or test
	private String api_version; // v2 or v1 in the server url

	// authentication bits
	private String tokenURL; // Mgmt API OAuth token endpoint
	private String mfaToken; // Mgmt API OAuth MFA - TOTP
	private String clientId; //Mgmt API OAuth Client Id (optional)
	private String clientSecret; //Mgmt API OAuth Client Secret (optional)
	private String bearerToken; //Mgmt API OAuth Token
	private String refreshToken; //Mgmt API OAuth Refresh Token
	private String authType; // Mgmt API Auth Type oauth|basic
	private String credential_user;
	private String credential_pwd; //

	// packaging configuration
	private String bundle_zip_full_path;
	private String profileId; //Profile id as in parent pom

	private EnumSet<ActionFlags> actions = EnumSet.noneOf(ActionFlags.class);

	private Long delay;
	private Long delayOverride;
	private HttpClient apacheHttpClient;
	
	//For Proxy
	private boolean hasProxy;
	private String proxyProtocol;
	private String proxyServer;
	private int proxyPort;
	private String proxyUsername;
	private String proxyPassword;

	public String getOrg() {
		return org;
	}

	public void setOrg(String org) {
		this.org = org;
	}

	public String getCredential_user() {
		return credential_user;
	}

	public void setCredential_user(String credential_user) {
		this.credential_user = credential_user;
	}

	public String getCredential_pwd() {
		return credential_pwd;
	}

	public void setCredential_pwd(String credential_pwd) {
		this.credential_pwd = credential_pwd;
	}

	public String getHostUrl() {
		return hostURL;
	}

	public void setHostUrl(String host) {
		this.hostURL = host;
	}

	public String getTokenUrl() {
		return tokenURL;
	}

	public void setTokenUrl(String url) {
		this.tokenURL = url;
	}

	public String getMFAToken() {
		return mfaToken;
	}

	public void setMFAToken(String otp) {
		this.mfaToken = otp;
	}

	public String getClientId() {
		return this.clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getClientSecret() {
		return this.clientSecret;
	}

	public void setClientSecret(String clientSecret) {
		this.clientSecret = clientSecret;
	}

	public String getBearerToken() {
		return this.bearerToken;
	}

	public void setBearerToken(String token) {
		this.bearerToken = token;
	}

	public String getRefreshToken() {
		return this.refreshToken;
	}

	public void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}

	public String getAuthType() {
		return authType;
	}

	public void setAuthType(String flag) {
		this.authType = flag;
	}

	public String getEnvironment() {
		return environment;
	}

	public void setEnvironment(String environment) {
		this.environment = environment;
	}

	public String getApi_version() {
		return api_version;
	}

	public void setApi_version(String api_version) {
		this.api_version = api_version;
	}

	public String getBundle_zip_full_path() {
		return bundle_zip_full_path;
	}

	public void setBundle_zip_full_path(String bundle_zip_full_path) {
		this.bundle_zip_full_path = bundle_zip_full_path;
	}

	/**
	 * @return the profileid
	 */
	public String getProfileId() {
		return profileId;
	}

	/**
	 * @param id the id to set
	 */
	public void setProfileId(String id) {
		this.profileId = id;
	}

	public Long getDelay() {
		return delay;
	}

	public void setDelay(Long delay) {
		this.delay = delay;
	}

	public Long getDelayOverride() {
		return delayOverride;
	}

	public void setDelayOverride(Long delayOverride) {
		this.delayOverride = delayOverride;
	}

	/**
	 * Enum set of actions configured for the profile.
	 *
	 * @return action set representing configuration of the options parameter
	 */
	public EnumSet<ActionFlags> getActions() {
		return actions;
	}

	/**
	 * Set the actions that the rest client needds to yield to. The operation will clear the
	 * current configuration and replaces it with collection passed.
	 *
	 * @param actions an enum set of action flags
	 */
	public void setActions(EnumSet<ActionFlags> actions) {
		this.actions.clear();
		this.actions.addAll(actions);
	}

	/**
	 * Add an action to the profile.
	 *
	 * @param actions the action(s) to add
	 */
	public void addAction(ActionFlags... actions) {
		for (ActionFlags action : actions) {
			if (!this.actions.contains(action)) {
				this.actions.add(action);
			}
		}
	}

	public boolean isOverride() {
		return actions.contains(ActionFlags.OVERRIDE);
	}

	public boolean isValidate() {
		return actions.contains(ActionFlags.VALIDATE);
	}

	public boolean isForce() {
		return actions.contains(ActionFlags.FORCE);
	}

	public boolean isUpdate() {
		return actions.contains(ActionFlags.UPDATE);
	}

	public boolean isClean() {
		return actions.contains(ActionFlags.CLEAN);
	}

	public boolean isInactive() {
		return actions.contains(ActionFlags.INACTIVE);
	}

	public void setApacheHttpClient(HttpClient apacheHttpClient) {
		this.apacheHttpClient = apacheHttpClient;
	}

	public HttpClient getApacheHttpClient() {
		return apacheHttpClient;
	}

	/**
	 * @return the proxyProtocol
	 */
	public String getProxyProtocol() {
		return proxyProtocol;
	}

	/**
	 * @param proxyProtocol the proxyProtocol to set
	 */
	public void setProxyProtocol(String proxyProtocol) {
		this.proxyProtocol = proxyProtocol;
	}

	/**
	 * @return the proxyServer
	 */
	public String getProxyServer() {
		return proxyServer;
	}

	/**
	 * @param proxyServer the proxyServer to set
	 */
	public void setProxyServer(String proxyServer) {
		this.proxyServer = proxyServer;
	}

	/**
	 * @return the proxyPort
	 */
	public int getProxyPort() {
		return proxyPort;
	}

	/**
	 * @param proxyPort the proxyPort to set
	 */
	public void setProxyPort(int proxyPort) {
		this.proxyPort = proxyPort;
	}

	/**
	 * @return the hasProxy
	 */
	public boolean getHasProxy() {
		return hasProxy;
	}

	/**
	 * @param hasProxy the hasProxy to set
	 */
	public void setHasProxy(boolean hasProxy) {
		this.hasProxy = hasProxy;
	}
	
	/**
	 * @return the proxyUsername
	 */
	public String getProxyUsername() {
		return proxyUsername;
	}

	/**
	 * @param proxyUsername the proxyUsername to set
	 */
	public void setProxyUsername(String proxyUsername) {
		this.proxyUsername = proxyUsername;
	}
	
	/**
	 * @return the proxyPassword
	 */
	public String getProxyPassword() {
		return proxyPassword;
	}

	/**
	 * @param proxyPassword the proxyPassword to set
	 */
	public void setProxyPassword(String proxyPassword) {
		this.proxyPassword = proxyPassword;
	}
	
}
