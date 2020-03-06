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

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.io.File;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultProxyAuthenticationHandler;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Proxy;
import org.apache.maven.settings.Settings;
import org.apache.maven.settings.crypto.DefaultSettingsDecryptionRequest;
import org.apache.maven.settings.crypto.SettingsDecrypter;
import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.apigee.buildTools.enterprise4g.utils.ServerProfile;

public abstract class GatewayAbstractMojo extends AbstractMojo implements Contextualizable {

	static Logger logger = LoggerFactory.getLogger(GatewayAbstractMojo.class);
	protected static final Pattern URL_PARSE_REGEX = Pattern.compile("^(http[s]?)://([^:/?#]*).*$");

	/**
	 * The project being built
	 * 
	 * @parameter default-value="${project}"
	 * @readonly
	 * @required
	 */
	protected MavenProject project;

	/**
	 * The Maven Session
	 * 
	 * @parameter default-value="${session}"
	 * @readonly
	 */
	protected MavenSession session;

	/**
	 * The Maven settings
	 * 
	 * @parameter default-value="${settings}"
	 * @readonly
	 */
	protected Settings settings;

	/**
	 * Injecting the underlying IoC container to access maven configurations
	 */
	@Requirement
	protected PlexusContainer container;

	/**
	 * Injecting the settings decrypter module that allows us to access decrypted
	 * properties
	 */
	@Requirement
	protected SettingsDecrypter settingsDecrypter;

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
	 * Mgmt API OAuth token endpoint
	 * 
	 * @parameter expression="${apigee.tokenurl}"
	 *            default-value="https://login.apigee.com/oauth/token"
	 */
	private String tokenURL;

	/**
	 * Mgmt API OAuth MFA - TOTP
	 * 
	 * @parameter expression="${apigee.mfatoken}"
	 */
	private String mfaToken;

	/**
	 * Mgmt API authn type
	 * 
	 * @parameter expression="${apigee.authtype}" default-value="basic"
	 */
	private String authType;

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
	 * Shared Flow api type
	 * 
	 * @parameter expression="${apigee.apitype}"
	 */
	private String apiType;

	/**
	 * Gateway org name
	 * 
	 * @parameter expression="${apigee.org}"
	 */
	private String orgName;

	/**
	 * Gateway bearer token
	 * 
	 * @parameter expression="${apigee.bearer}"
	 */
	private String bearer;

	/**
	 * Gateway refresh token
	 * 
	 * @parameter expression="${apigee.refresh}"
	 */
	private String refresh;

	/**
	 * Gateway OAuth clientId
	 * 
	 * @parameter expression="${apigee.clientid}"
	 */
	private String clientid;

	/**
	 * Gateway OAuth clientSecret
	 * 
	 * @parameter expression="${apigee.clientsecret}"
	 */
	private String clientsecret;

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
	 * Gateway revision
	 * 
	 * @parameter expression="${apigee.revision}"
	 */
	private Long revision;

	/**
	 * Skip running this plugin. Default is false.
	 *
	 * @parameter default-value="false"
	 */
	private boolean skip = false;

	public ServerProfile buildProfile;

	public GatewayAbstractMojo() {
		super();

	}

	/**
	 * {@inheritDoc}
	 */
	public void contextualize(Context context) throws ContextException {
		container = (PlexusContainer) context.get(PlexusConstants.PLEXUS_KEY);
		if (container.hasComponent(SettingsDecrypter.class)) {
			try {
				settingsDecrypter = container.lookup(SettingsDecrypter.class);
			} catch (ComponentLookupException e) {
				logger.warn("Failed to lookup build in maven component session descrupter.", e);
			}
		}
	}

	public ServerProfile getProfile() {
		this.buildProfile = new ServerProfile();
		this.buildProfile.setOrg(this.orgName);
		this.buildProfile.setApplication(this.projectName);
		this.buildProfile.setApi_version(this.apiVersion);
		this.buildProfile.setApi_type(this.apiType);
		this.buildProfile.setHostUrl(this.hostURL);
		this.buildProfile.setTokenUrl(this.tokenURL);
		this.buildProfile.setMFAToken(this.mfaToken);
		this.buildProfile.setAuthType(this.authType);
		this.buildProfile.setEnvironment(this.deploymentEnv);
		this.buildProfile.setBearerToken(this.bearer);
		this.buildProfile.setRefreshToken(this.refresh);
		this.buildProfile.setCredential_user(this.userName);
		this.buildProfile.setCredential_pwd(this.password);
		this.buildProfile.setClientId(this.clientid);
		this.buildProfile.setClientSecret(this.clientsecret);
		this.buildProfile.setProfileId(this.id);
		this.buildProfile.setOptions(this.options);
		this.buildProfile.setDelay(this.delay);
		this.buildProfile.setOverridedelay(this.overridedelay);
		this.buildProfile.setRevision(this.revision);

		// process proxy for management api endpoint
		Proxy mavenProxy = getProxy(settings, hostURL);
		if (mavenProxy != null) {
			logger.info("set proxy to " + mavenProxy.getHost() + ":" + mavenProxy.getPort());
			DefaultHttpClient httpClient = new DefaultHttpClient();
			HttpHost proxy = new HttpHost(mavenProxy.getHost(), mavenProxy.getPort());
			httpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
			if (isNotBlank(mavenProxy.getNonProxyHosts())) {
				//System.setProperty("http.nonProxyHosts", mavenProxy.getNonProxyHosts().replaceAll("[,;]", "|"));
				// TODO selector based proxy
			}
			if (isNotBlank(mavenProxy.getUsername()) && isNotBlank(mavenProxy.getPassword())) {
				logger.debug("set proxy credentials");
				httpClient.setProxyAuthenticationHandler(new DefaultProxyAuthenticationHandler());
				httpClient.getCredentialsProvider().setCredentials(
						new AuthScope(mavenProxy.getHost(), mavenProxy.getPort()),
						new UsernamePasswordCredentials(mavenProxy.getUsername(), mavenProxy.getPassword()));
			}
			buildProfile.setApacheHttpClient(httpClient);
		}
		return buildProfile;
	}

	/**
	 *  Get the proxy configuration from the maven settings
	 * 
	 *  @param settings the maven settings   
	 *  @param host the host name of the apigee edge end point
	 *  @return proxy or null if none was configured or the host was non-proxied      
	 */
	protected Proxy getProxy(final Settings settings, final String host) {
		if (settings == null) {
			return null;
		}
		List<Proxy> proxies = settings.getProxies();
		if (proxies == null || proxies.isEmpty()) {
			return null;
		}

		String protocol = "https";
		String hostname = host;

		// check if protocol is present, if not assume https
		Matcher matcher = URL_PARSE_REGEX.matcher(host);
		if (matcher.matches()) {
			protocol = matcher.group(1);
			hostname = matcher.group(2);
		}

		// search active proxy
		for (Proxy proxy : proxies) {
			if (proxy.isActive() && protocol.equalsIgnoreCase(proxy.getProtocol()) && !matchNonProxy(proxy, hostname)) {
				if (settingsDecrypter != null) {
					return settingsDecrypter.decrypt(new DefaultSettingsDecryptionRequest(proxy)).getProxy();
				} else {
					logger.warn("Maven did not inject SettingsDecrypter, " +
							"proxy may contain an encrypted password, which cannot be " +
							"used to setup the REST client.");
					return proxy;
				}
			}
		}
		return null;
	}

	/**
	 * Check hostname that matched nonPRoxu setting
	 * @param proxy	 Maven Proxy must not be null
	 * @param hostname 
	 * @return
	 */
	protected boolean matchNonProxy(final Proxy proxy, final String hostname) {

		// code from org.apache.maven.plugins.site.AbstractDeployMojo#getProxyInfo
		final String nonProxyHosts = proxy.getNonProxyHosts();
		if (null != nonProxyHosts) {
			final String[] nonProxies = nonProxyHosts.split("(,)|(;)|(\\|)");
			if (null != nonProxies) {
				for (final String nonProxyHost : nonProxies) {
					//if ( StringUtils.contains( nonProxyHost, "*" ) )
					if (null != nonProxyHost && nonProxyHost.contains("*")) {
						// Handle wildcard at the end, beginning or middle of the nonProxyHost
						final int pos = nonProxyHost.indexOf('*');
						String nonProxyHostPrefix = nonProxyHost.substring(0, pos);
						String nonProxyHostSuffix = nonProxyHost.substring(pos + 1);
						// prefix*
						if (!isBlank(nonProxyHostPrefix) && hostname.startsWith(nonProxyHostPrefix) && isBlank(nonProxyHostSuffix)) {
							return true;
						}
						// *suffix
						if (isBlank(nonProxyHostPrefix) && !isBlank(nonProxyHostSuffix) && hostname.endsWith(nonProxyHostSuffix)) {
							return true;
						}
						// prefix*suffix
						if (!isBlank(nonProxyHostPrefix) && hostname.startsWith(nonProxyHostPrefix)
								&& !isBlank(nonProxyHostSuffix) && hostname.endsWith(nonProxyHostSuffix)) {
							return true;
						}
					} else if (hostname.equals(nonProxyHost)) {
						return true;
					}
				}
			}
		}
		return false;
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
		return this.baseDirectory + File.separator + "target" + File.separator + this.projectName + "-"
				+ this.projectVersion + ".zip";
	}

	public String getBaseDirectoryPath() {
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

	public Long getRevision() {
		return this.revision;
	}

	public void setRevision(Long revision) {
		this.revision = revision;
	}

}
