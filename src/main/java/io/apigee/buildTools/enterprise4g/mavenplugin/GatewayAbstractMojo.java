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

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.io.File;
import java.util.EnumSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultProxyAuthenticationHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
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

import io.apigee.buildTools.enterprise4g.rest.ActionFlags;
import io.apigee.buildTools.enterprise4g.rest.Bundle;
import io.apigee.buildTools.enterprise4g.utils.ServerProfile;

public abstract class GatewayAbstractMojo extends AbstractMojo implements Contextualizable {
	
	static Logger logger = LogManager.getLogger(GatewayAbstractMojo.class);
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
	 * The Maven session
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
	 * Injecting the underlying IoC container to access maven configuration.
	 */
	@Requirement
	protected PlexusContainer container;

	/**
	 * Injecting the settings decrypter module that allows us to access decrypted properties.
	 */
	@Requirement
	protected SettingsDecrypter settingsDecrypter;

	/**
	 * Directory containing the build files.
	 *
	 * @parameter default-value="${project.build.directory}"
	 * @readonly
	 */
	private File buildDirectory;

	/**
	 * Base directory of the project.
	 *
	 * @parameter default-value="${project.basedir}"
	 * @readonly
	 */
	private File baseDirectory;

	/**
	 * Project Name
	 *
	 * @parameter default-value="${project.name}"
	 * @readonly
	 */
	private String projectName;

	/**
	 * Project version
	 *
	 * @parameter default-value="${project.version}"
	 */
	private String projectVersion;

	/**
	 * Project artifact id
	 *
	 * @parameter default-value="${project.artifactId}"
	 */
	private String artifactId;

	/**
	 * Profile id
	 *
	 * @parameter property="apigee.profile"
	 */
	private String id;

	/**
	 * Gateway host URL
	 *
	 * @parameter property="apigee.hosturl" default-value="https://api.enterprise.apigee.com"
	 */
	private String hostURL;

	/**
	 * Mgmt API OAuth token endpoint
	 *
	 * @parameter property="apigee.tokenurl" default-value="https://login.apigee.com/oauth/token"
	 */
	private String tokenURL;

	/**
	 * Mgmt API OAuth MFA - TOTP
	 *
	 * @parameter property="apigee.mfatoken"
	 */
	private String mfaToken;

	/**
	 * Mgmt API authn type
	 *
	 * @parameter property="apigee.authtype" default-value="basic"
	 */
	private String authType;

	/**
	 * Gateway env profile
	 *
	 * @parameter property="apigee.env" default-value="${apigee.profile}"
	 */
	private String deploymentEnv;

	/**
	 * Gateway api version
	 *
	 * @parameter property="apigee.apiversion" default-value="v1"
	 */
	private String apiVersion;

	/**
	 * The type of API to deploy, value can either be apiproxy or sharedflow.
	 *
	 * @parameter property="apigee.apitype" default-value="apiproxy"
	 */
	private String apiType;

	/**
	 * Gateway org name
	 *
	 * @parameter property="apigee.org"
	 */
	private String orgName;

	/**
	 * Gateway bearer token
	 *
	 * @parameter property="apigee.bearer"
	 */
	private String bearer;

	/**
	 * Gateway refresh token
	 *
	 * @parameter property="apigee.refresh"
	 */
	private String refresh;

	/**
	 * Gateway OAuth clientId
	 *
	 * @parameter property="apigee.clientid"
	 */
	private String clientid;

	/**
	 * Gateway OAuth clientSecret
	 *
	 * @parameter property="apigee.clientsecret"
	 */
	private String clientsecret;

	/**
	 * Gateway host username
	 *
	 * @parameter property="apigee.username"
	 */
	private String username;

	/**
	 * Gateway host password
	 *
	 * @parameter property="apigee.password"
	 */
	private String password;

	/**
	 * Build option is now deprecated. Use {@link #options} instead.
	 *
	 * @parameter property="build.option"
	 * @deprecated use options instead
	 */
	private String buildOption;

	/**
	 * A comma or space separated list of option codes.
	 * <p>
	 * Possible values refer to {@link ActionFlags}
	 *
	 * @parameter property="apigee.options"
	 */
	private String options;

	/**
	 * Delay in milliseconds used to introduce a wait time after executing an API call to Apigee Edge.
	 *
	 * @parameter property="apigee.delay" default-value="0"
	 */
	private Long delay;

	/**
	 * Delay used in the activate bundle API call to change the default Apigee Edge deployment mechanism. The value is set in seconds.
	 * see http://docs.apigee.com/api-services/content/deploy-api-proxies-using-management-api
	 *
	 * @parameter property="apigee.override.delay" default-value="0"
	 */
	private Long overrideDelay;

	/**
	 * The revision number of the bundle to be updated. Only works in conjunction with update or delete actions.
	 *
	 * @parameter property="apigee.revision"
	 */
	private Long revision;

	/**
	 * Skip running this plugin.
	 * Default is false.
	 *
	 * @parameter default-value="false"
	 */
	private boolean skip = false;

	protected GatewayAbstractMojo() {
	}

	/**
	 * Create a bundle profile from the maven pluin configuration.
	 *
	 * @return the application/proxy bundle as configured
	 */
	protected Bundle createBundle() {
		String name = isNotBlank(projectName) ? projectName : artifactId;
		// default to api proxy if
		Bundle.Type type = (isNotBlank(apiType) && apiType.startsWith("sharedflow")) ?
				Bundle.Type.SHAREDFLOW : Bundle.Type.APIPROXY;
		return new Bundle(name, type, revision);
	}

	/**
	 * Process the provided options property and create a bit flag type action set.
	 *
	 * @return a collection of action parameters provided
	 */
	protected EnumSet<ActionFlags> parseOptions(String options) {
		// parse action flags
		EnumSet<ActionFlags> actions = EnumSet.noneOf(ActionFlags.class);
		if (options != null) {
			String[] opts = options.split(",");
			for (String opt : opts) {
				ActionFlags flag = ActionFlags.valueOfIgnoreCase(opt);
				if (flag != null) {
					actions.add(flag);
				}
			}
		}
		return actions;
	}

	/**
	 * Create a server profile from the mojo configuration provided.
	 *
	 * @return a valid server profile
	 */
	public ServerProfile createProfile() throws MojoExecutionException {
		return createProfile(true);
	}

	/**
	 * Create a server profile from the mojo configuration provided.
	 * <p>
	 * TODO this is a smell, why do I have to fiddle with auth fields when I just want to build/configure a build artifact.
	 *
	 * @param validateAuthentication A flag to indicate if the server profile must include authentication details
	 *
	 * @return a valid server profile (with or without auth information - yuck)
	 */
	public ServerProfile createProfile(boolean validateAuthentication) throws MojoExecutionException {
		ServerProfile profile = new ServerProfile();

		// basic stuff
		if (isBlank(orgName)) {
			throw new MojoExecutionException("Parameter apigee.org must be provided");
		} else {
			profile.setOrg(orgName);
		}

		profile.setApi_version(apiVersion);

		profile.setHostUrl(hostURL);
		profile.setEnvironment(deploymentEnv);
		profile.setProfileId(id);

		// setup legacy build options only if no options have been provided
		if (isBlank(options) && isNotBlank(buildOption)) {
			if ("deploy-inactive".equals(buildOption)) {
				logger.warn("Note: -Dbuild.option=deploy-inactive is Deprecated, use -Dapigee.options=inactive instead");
				profile.addAction(ActionFlags.INACTIVE, ActionFlags.VALIDATE);
			} else if ("undeploy".equals(buildOption)) {
				// FIXME The original code only undeploys the module but does not delete it, there is no equivalent in the current option set.
				// old code did this: client.deactivateBundle(bundle);
				logger.warn("Note: -Dbuild.option=undeploy is Deprecated, use -Dapigee.options=clean instead");
				profile.addAction(ActionFlags.CLEAN);
			} else if ("delete".equals(buildOption)) {
				profile.addAction(ActionFlags.CLEAN);
			}
		} else {
			profile.setActions(parseOptions(options));
		}

		profile.setDelay(delay);
		profile.setDelayOverride(overrideDelay);

		// process auth settings
		if (validateAuthentication) {
			profile.setTokenUrl(tokenURL);
			profile.setMFAToken(mfaToken);
			profile.setAuthType(authType);
			profile.setBearerToken(bearer);
			profile.setRefreshToken(refresh);
			profile.setCredential_user(username);
			profile.setCredential_pwd(password);
			profile.setClientId(clientid);
			profile.setClientSecret(clientsecret);
			// TODO check auth type in [oauth|basic]
			if ("basic".equalsIgnoreCase(profile.getAuthType()) &&
					// basic auth requires both username and password
					(isBlank(profile.getCredential_user()) || isBlank(profile.getCredential_pwd()))) {
				throw new MojoExecutionException("Username and password must be provided for basic authentication.");
			} //else if ("oauth".equalsIgnoreCase(profile.getAuthType()) &&
					// either one is good enough
				//	(isBlank(profile.getBearerToken()) && isBlank(profile.getRefreshToken()))) {
				//throw new MojoExecutionException("A bearer or refresh token must be provided for token based authentication.");
			//}

			// process proxy for management api endpoint
			Proxy mavenProxy = getProxy(settings, hostURL);
			if (mavenProxy != null) {
				logger.info("set proxy to " + mavenProxy.getHost() + ":" + mavenProxy.getPort());
				DefaultHttpClient httpClient = new DefaultHttpClient();
				HttpHost proxy = new HttpHost(mavenProxy.getHost(), mavenProxy.getPort());
				httpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);

				if (isNotBlank(mavenProxy.getNonProxyHosts())) {
//				System.setProperty("http.nonProxyHosts", mavenProxy.getNonProxyHosts().replaceAll("[,;]", "|"));
					// TODO selector based proxy
				}

				if (isNotBlank(mavenProxy.getUsername()) && isNotBlank(mavenProxy.getPassword())) {
					logger.debug("set proxy credentials");
					httpClient.setProxyAuthenticationHandler(new DefaultProxyAuthenticationHandler());
					httpClient.getCredentialsProvider().setCredentials(
							new AuthScope(mavenProxy.getHost(), mavenProxy.getPort()),
							new UsernamePasswordCredentials(mavenProxy.getUsername(), mavenProxy.getPassword())
					);
					profile.setProxyUsername(mavenProxy.getUsername());
					profile.setProxyPassword(mavenProxy.getPassword());
				}
				profile.setApacheHttpClient(httpClient);
				
				//Set Proxy configurations
				profile.setHasProxy(true);
				profile.setProxyProtocol(mavenProxy.getProtocol());
				profile.setProxyServer(mavenProxy.getHost());
				profile.setProxyPort(mavenProxy.getPort());
			}

		}

		return profile;
	}

	public void setBaseDirectory(File baseDirectory) {
		this.baseDirectory = baseDirectory;
	}

	public String getBuildDirectory() {
		return buildDirectory.getAbsolutePath();
	}

	public String getApplicationBundlePath() {
		StringBuilder sb = new StringBuilder();
		sb.append(buildDirectory.getPath()).append(File.separator);
		if (projectName != null && !projectName.trim().isEmpty()) {
			sb.append(projectName);
		} else {
			sb.append(artifactId);
		}
		sb.append('-').append(projectVersion);
		// if profile id is not set we dont want null in the bundle's name
		if (id != null && !id.trim().isEmpty()) {
			sb.append('-').append(id);
		}
		sb.append(".zip");
		return sb.toString();
	}

	public String getBaseDirectoryPath() {
		return baseDirectory.getAbsolutePath();
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
		return delay;
	}

	public void setDelay(Long delay) {
		this.delay = delay;
	}

	public Long getOverrideDelay() {
		return overrideDelay;
	}

	public void setOverrideDelay(Long overrideDelay) {
		this.overrideDelay = overrideDelay;
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
				logger.warn("Failed to lookup build in maven component session decrypter.", e);
			}
		}
	}

	/**
	 * Get the proxy configuration from the maven settings
	 *
	 * @param settings the maven settings
	 * @param host     the host name of the apigee edge endpoint
	 *
	 * @return proxy or null if none was configured or the host was non-proxied
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
	 * Check hostname that matched nonProxy setting
	 *
	 * @param proxy    Maven Proxy. Must not null
	 * @param hostname
	 *
	 * @return matching result. true: match nonProxy
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


}
