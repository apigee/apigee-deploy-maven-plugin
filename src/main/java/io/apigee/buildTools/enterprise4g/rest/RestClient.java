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
package io.apigee.buildTools.enterprise4g.rest;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import org.apache.maven.plugin.MojoExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.apigee.mgmtapi.sdk.client.MgmtAPIClient;
import com.apigee.mgmtapi.sdk.model.AccessToken;
import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.UrlEncodedContent;
import com.google.api.client.http.apache.ApacheHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.GenericData;
import com.google.api.client.util.Key;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import io.apigee.buildTools.enterprise4g.utils.PrintUtil;
import io.apigee.buildTools.enterprise4g.utils.ServerProfile;
import io.apigee.buildTools.enterprise4g.utils.StringToIntComparator;

public class RestClient {

	public static final String STATE_UNDEPLOYED = "undeployed";
	public static final String STATE_DEPLOYED = "deployed";
	public static final String STATE_ERROR = "error";
	public static final String STATE_IMPORTED = "imported";
	private static final transient Logger log = LoggerFactory.getLogger(RestClient.class);

	// FIXME access token should be handled as state and be tracking access credentials between module invocations
	@Deprecated
	static String accessToken = null;

	/**
	 * the factory method used by the google http client to setup JSON parsing
	 */
	private JsonFactory JSON_FACTORY = new GsonFactory();

	/**
	 * HTTP request factory used to construct the http requests
	 */
	private HttpRequestFactory requestFactory;

	/**
	 * the JSON parser used to interact with the rest responses
	 */
	private Gson gson = new GsonBuilder().setPrettyPrinting().create();

	/**
	 * The configuration used by the rest client to access the Apigee management and authentication APIs.
	 */
	private ServerProfile profile;

	public RestClient(ServerProfile profile) {
		this.profile = profile;

		HttpTransport httpTransport;

		if (profile.getApacheHttpClient() != null) {
			httpTransport = new ApacheHttpTransport(profile.getApacheHttpClient());
		} else {
			httpTransport = new NetHttpTransport();
		}

		requestFactory = httpTransport.createRequestFactory(new HttpRequestInitializer() {
			// @Override
			public void initialize(HttpRequest request) {
				request.setParser(JSON_FACTORY.createJsonObjectParser());
				XTrustProvider.install();
				// FIXME this is bad - Install the all-trusting host name verifier
				HttpsURLConnection.setDefaultHostnameVerifier(new FakeHostnameVerifier());
			}
		});

	}

	/**
	 * This method is used to validate the Bearer token. It validates the source and the expiration and if the token is about to expire in 30 seconds, set as invalid token
	 *
	 * @param accessToken the access token to test
	 * @param profile     the server profile
	 * @param clientId    the clientId used to retrieve access tokens
	 *
	 * @return true if the token is valid and false otherwise
	 *
	 * @throws JWTDecodeException if the method failed to parse the supplied token
	 */
	private static boolean isValidBearerToken(String accessToken, ServerProfile profile, String clientId) throws JWTDecodeException {
		boolean isValid = false;
		JWT jwt = JWT.decode(accessToken);
		String jwtClientId = jwt.getClaim("client_id").asString();
		String jwtEmailId = jwt.getClaim("email").asString();
		long jwtExpiresAt = jwt.getExpiresAt().getTime() / 1000;
		long difference = jwtExpiresAt - (System.currentTimeMillis() / 1000);
		if (jwt != null && jwtClientId != null && jwtClientId.equals(clientId)
				&& jwtEmailId != null && jwtEmailId.equalsIgnoreCase(profile.getCredential_user())
				&& profile.getTokenUrl().contains(jwt.getIssuer())
				&& difference >= 30) {
			isValid = true;
		}
		return isValid;
	}

	public ServerProfile getProfile() {
		return profile;
	}

	/**
	 * OAuth token acquisition for calling management APIs
	 * Access Token expiry 1799 sec = 30 mins long enough to finish any maven task
	 * MFA Token: TOTP expires in 30 secs. User needs to give a token with some validity
	 */
	private HttpResponse executeAPI(ServerProfile profile, HttpRequest request) throws IOException {
		HttpHeaders headers = request.getHeaders();
		MgmtAPIClient client = new MgmtAPIClient(profile);
		String mfaToken = profile.getMFAToken();
		String tokenUrl = profile.getTokenUrl();
		String mgmtAPIClientId = isNotBlank(profile.getClientId()) ? profile.getClientId() : "edgecli";
		String mgmtAPIClientSecret = isNotBlank(profile.getClientSecret()) ? profile.getClientSecret() : "edgeclisecret";

		/**** Basic Auth - Backward compatibility ****/
		if ("basic".equalsIgnoreCase(profile.getAuthType())) {
			headers.setBasicAuthentication(profile.getCredential_user(), profile.getCredential_pwd());
			log.info(PrintUtil.formatRequest(request));
			return request.execute();
		}

		/**** OAuth ****/
		if (isNotBlank(profile.getBearerToken())) {
			//Need to validate access token only if refresh token is provided.
			//If access token is not valid, create a bearer token using the refresh token
			//If access token is valid, use that
			accessToken = (accessToken != null) ? accessToken : profile.getBearerToken();
			if (profile.getRefreshToken() != null && !profile.getRefreshToken().equalsIgnoreCase("")) {
				if (isValidBearerToken(accessToken, profile, mgmtAPIClientId)) {
					log.info("Access Token valid");
					headers.setAuthorization("Bearer " + accessToken);
				} else {
					try {
						AccessToken token = null;
						log.info("Access token not valid so acquiring new access token using Refresh Token");
						token = client.getAccessTokenFromRefreshToken(
								tokenUrl,
								mgmtAPIClientId, mgmtAPIClientSecret,
								profile.getRefreshToken());
						log.info("New Access Token acquired");
						accessToken = token.getAccess_token();
						headers.setAuthorization("Bearer " + accessToken);
					} catch (Exception e) {
						log.error(e.getMessage());
						throw new IOException(e.getMessage());
					}
				}
			}
			//if refresh token is not passed, validate the access token and use it accordingly
			else {
				log.info("Validating the access token passed");
				if (isValidBearerToken(profile.getBearerToken(), profile, mgmtAPIClientId)) {
					log.info("Access Token valid");
					accessToken = profile.getBearerToken();
					headers.setAuthorization("Bearer " + accessToken);
				} else {
					log.error("Access token not valid");
					throw new IOException("Access token not valid");
				}

			}
		} else if (accessToken != null) {
			// subsequent calls
			log.debug("Reusing mgmt API access token");
			headers.setAuthorization("Bearer " + accessToken);
		} else {
			log.info("Acquiring mgmt API token from " + tokenUrl);
			AccessToken token = null;
			if (mfaToken == null || mfaToken.length() == 0) {
				log.info("MFA token not provided. Skipping.");
				token = client.getAccessToken(
						tokenUrl,
						mgmtAPIClientId, mgmtAPIClientSecret,
						profile.getCredential_user(),
						profile.getCredential_pwd());
			} else {
				log.info("Making use of the MFA token provided.");
				token = client.getAccessToken(
						tokenUrl,
						mgmtAPIClientId, mgmtAPIClientSecret,
						profile.getCredential_user(),
						profile.getCredential_pwd(),
						profile.getMFAToken());
			}
			accessToken = token.getAccess_token();
			headers.setAuthorization("Bearer " + accessToken);
		}
		log.info(PrintUtil.formatRequest(request));
		return request.execute();
	}

	public void initMfa() throws IOException {

		// any simple get request can be used to - we just need to get an access token
		// whilst the mfatoken is still valid

		// trying to construct the URL like
		// https://api.enterprise.apigee.com/v1/organizations/apigee-cs/apis/taskservice/
		// success response is ignored
		if (accessToken == null) {
			log.info("initialising MFA");

			GenericUrl url = new GenericUrl(
					format("%s/%s/organizations/%s",
							profile.getHostUrl(),
							profile.getApi_version(),
							profile.getOrg()));

			HttpRequest restRequest = requestFactory.buildGetRequest(url);
			restRequest.setReadTimeout(0);
			HttpHeaders headers = new HttpHeaders();
			headers.setAccept("application/json");
			restRequest.setHeaders(headers);

			try {
				executeAPI(profile, restRequest);
				//ignore response - we just wanted the MFA initialised
				log.info("initialising MFA completed");
			} catch (HttpResponseException e) {
				log.error(e.getMessage());
				//throw error as there is no point in continuing
				throw e;
			}
		}
	}

	/**
	 * Retrieve the last revision of a given apiproxy or sharedflow.
	 *
	 * @param bundle the application bundle to check
	 *
	 * @return the highest revision number of the deployed bundle regardless of environment
	 *
	 * @throws IOException exception if something went wrong communicating with the rest endpoint
	 */
	public Long getLatestRevision(Bundle bundle) throws IOException {

		// trying to construct the URL like
		// https://api.enterprise.apigee.com/v1/organizations/apigee-cs/apis/taskservice/
		// response is like
		// {
		// "name" : "taskservice1",
		// "revision" : [ "1" ]
		// }

		GenericUrl url = new GenericUrl(
				format("%s/%s/organizations/%s/%s/%s/",
						profile.getHostUrl(),
						profile.getApi_version(),
						profile.getOrg(),
						bundle.getType().getPathName(),
						bundle.getName()));

		HttpRequest restRequest = requestFactory.buildGetRequest(url);
		restRequest.setReadTimeout(0);
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept("application/json");
		restRequest.setHeaders(headers);

		Long revision = null;

		try {
			HttpResponse response = executeAPI(profile, restRequest);
			AppRevision apprev = response.parseAs(AppRevision.class);
			Collections.sort(apprev.revision, new StringToIntComparator());
			revision = Long.parseLong(apprev.revision.get(0));
			log.info(PrintUtil.formatResponse(response, gson.toJson(apprev)));
		} catch (HttpResponseException e) {
			log.error(e.getMessage());
		}
		return revision;
	}

	/**
	 * Retrieve the last revision of a given apiproxy that is deployed in the environment specified in the profile.
	 *
	 * @param bundle the application bundle to check
	 *
	 * @return the revision number of the deployed bundle within the environment specified in the connection profile.
	 * If the bundle is not deployed in the environment, the result will be null.
	 *
	 * @throws IOException exception if something went wrong communicating with the rest endpoint
	 */
	public Long getDeployedRevision(Bundle bundle) throws IOException {

		BundleDeploymentConfig deployment1 = null;

		try {

			GenericUrl url = new GenericUrl(
					format("%s/%s/organizations/%s/%s/%s/deployments/",
							profile.getHostUrl(),
							profile.getApi_version(),
							profile.getOrg(),
							bundle.getType().getPathName(),
							bundle.getName()));

			HttpRequest restRequest = requestFactory.buildGetRequest(url);

			restRequest.setReadTimeout(0);
			restRequest.setHeaders(new HttpHeaders().setAccept("application/json"));

			HttpResponse response = executeAPI(profile, restRequest);
			deployment1 = response.parseAs(BundleDeploymentConfig.class);
			log.debug(PrintUtil.formatResponse(response, gson.toJson(deployment1)));

            /*if (deployment1 != null) {
				for (Environment env : deployment1.environment) {
                    if (env.name.equalsIgnoreCase(profile.getEnvironment()))
                        return env.revision.get(0).name;
                }
            }*/
			//Fix for https://github.com/apigee/apigee-deploy-maven-plugin/issues/53
			if (deployment1 != null) {
				for (Environment env : deployment1.environment) {
					if (env.name.equalsIgnoreCase(profile.getEnvironment())) {
						List<Revision> revisionList = env.revision;
						if (revisionList != null && revisionList.size() > 0) {
							List<String> revisions = new ArrayList<String>();
							for (Revision revision : revisionList) {
								revisions.add(revision.name);
							}
							Collections.sort(revisions, new StringToIntComparator());
							return Long.parseLong(revisions.get(0));
						}
					}
				}
			}

		} catch (HttpResponseException e) {
			log.error(e.getMessage());
		} catch (Exception e) {
			log.error(e.getMessage());
		}

		//This is not correct , it will always return the 1st env's deployed revision .
		//return deployment1.environment.get(0).revision.get(0).name;
		return null;

	}

	/**
	 * Import a bundle into the Apigee gateway.
	 *
	 * @param bundleFile reference to the local bundle file
	 * @param bundle     the bundle descriptor to use
	 *
	 * @return the revision of the uploaded bundle
	 *
	 * @throws IOException exception if something went wrong communicating with the rest endpoint
	 */
	public Long uploadBundle(String bundleFile, Bundle bundle) throws IOException {

		FileContent fContent = new FileContent("application/octet-stream",
				new File(bundleFile));
		//testing
		log.debug("URL parameters API Version {}", (profile.getApi_version()));
		log.debug("URL parameters URL {}", (profile.getHostUrl()));
		log.debug("URL parameters Org {}", (profile.getOrg()));
		log.debug("URL parameters App {}", bundle.getName());

		StringBuilder url = new StringBuilder();

		url.append(format("%s/%s/organizations/%s/%s?action=import&name=%s",
				profile.getHostUrl(),
				profile.getApi_version(),
				profile.getOrg(),
				bundle.getType().getPathName(),
				bundle.getName()));

		if (getProfile().isValidate()) {
			url.append("&validate=true");
		}

		HttpRequest restRequest = requestFactory.buildPostRequest(new GenericUrl(url.toString()), fContent);
		restRequest.setReadTimeout(0);
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept("application/json");
		restRequest.setHeaders(headers);

		Long result;
		try {
			HttpResponse response = executeAPI(profile, restRequest);
			AppConfig apiResult = response.parseAs(AppConfig.class);
			result = apiResult.getRevision();
			if (log.isInfoEnabled())
				log.info(PrintUtil.formatResponse(response, gson.toJson(apiResult)));
			applyDelay();
		} catch (HttpResponseException e) {
			log.error(e.getMessage(), e);
			throw new IOException(e.getMessage(), e);
		}

		return result;

	}

	/**
	 * Apply wait delay specified in the profile to the current thread.
	 */
	private void applyDelay() {
		if (profile.getDelay() != 0) {
			try {
				if (log.isDebugEnabled())
					log.debug("Apply delay of {} ms", profile.getDelay());
				Thread.sleep(profile.getDelay());
			} catch (InterruptedException e) {
				log.warn("RestClient wait delay was interrupted.", e);
			}
		}
	}

	/**
	 * Update a revsion of a bunlde with the supplied content.
	 *
	 * @param bundleFile reference to the local bundle file
	 * @param bundle     the bundle descriptor to use
	 *
	 * @return the revision of the uploaded bundle
	 *
	 * @throws IOException exception if something went wrong communicating with the rest endpoint
	 */
	public Long updateBundle(String bundleFile, Bundle bundle) throws IOException {

		FileContent fContent = new FileContent("application/octet-stream",
				new File(bundleFile));
		//System.out.println("\n\n\nFile path: "+ new File(bundleFile).getCanonicalPath().toString());
		log.debug("URL parameters API Version {}", profile.getApi_version());
		log.debug("URL parameters URL {}", profile.getHostUrl());
		log.debug("URL parameters Org {}", profile.getOrg());
		log.debug("URL parameters App {}", bundle.getName());

		StringBuilder url = new StringBuilder();

		url.append(format("%s/%s/organizations/%s/%s/%s/revisions/%d",
				profile.getHostUrl(),
				profile.getApi_version(),
				profile.getOrg(),
				bundle.getType().getPathName(),
				bundle.getName(),
				bundle.getRevision()));

		if (getProfile().isValidate()) {
			url.append("?validate=true");
		}

		HttpRequest restRequest = requestFactory.buildPostRequest(new GenericUrl(url.toString()), fContent);
		restRequest.setReadTimeout(0);
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept("application/json");
		restRequest.setHeaders(headers);
		Long result;
		try {
			HttpResponse response = executeAPI(profile, restRequest);
			AppConfig appconf = response.parseAs(AppConfig.class);
			result = appconf.getRevision();
			if (log.isInfoEnabled())
				log.info(PrintUtil.formatResponse(response, gson.toJson(appconf)));
			applyDelay();
		} catch (HttpResponseException e) {
			log.error(e.getMessage());
			throw new IOException(e.getMessage());
		}

		return result;

	}

	public String deactivateBundle(Bundle bundle) throws IOException {
		BundleActivationConfig deployment1 = new BundleActivationConfig();
		try {

			Long existingRevision = getDeployedRevision(bundle);

			if (existingRevision != null) { //  there are active revisions deployment then undeploy
				log.info("De-activating Version: " + existingRevision + " For Env Profile: " + profile.getEnvironment());

				GenericUrl url = new GenericUrl(
						format("%s/%s/organizations/%s/environments/%s/%s/%s/revisions/%d/deployments",
								profile.getHostUrl(),
								profile.getApi_version(),
								profile.getOrg(),
								profile.getEnvironment(),
								bundle.getType().getPathName(),
								bundle.getName(),
								existingRevision
						));

				HttpRequest undeployRestRequest = requestFactory.buildDeleteRequest(url);
				undeployRestRequest.setReadTimeout(0);
				HttpHeaders headers = new HttpHeaders();
				headers.setAccept("application/json");
				undeployRestRequest.setHeaders(headers);

				HttpResponse response = null;
				response = executeAPI(profile, undeployRestRequest);
				deployment1 = response.parseAs(BundleActivationConfig.class);
				if (log.isInfoEnabled())
					log.info(PrintUtil.formatResponse(response, gson.toJson(deployment1)));
				applyDelay();
			} else {
				//If there are no existing active revisions
				deployment1.state = STATE_UNDEPLOYED;
			}

		} catch (HttpResponseException e) {
			log.error(e.getMessage());
			//deployment1.state = "No application was in deployed state";
			deployment1.state = STATE_ERROR;

		} catch (Exception e) {
			log.error(e.getMessage());
			//deployment1.state = "No application was in deployed state";
			deployment1.state = STATE_ERROR;
		} finally {
			//Rechecking only if we supply force option.  Checking if still any active revision exists
			if (getProfile().isForce()) {
				Long anyExistingRevision = null;
				try {
					log.info("Checking if any deployed version still exists for Env Profile: " + profile.getEnvironment());
					anyExistingRevision = getDeployedRevision(bundle);
				} catch (IOException e) {
					//deployment1.state = "\nNo application is in deployed state\n";
					log.error("Application couldn't be undeployed :: " + anyExistingRevision, e);
					deployment1.state = STATE_ERROR;
				}
				//check  if there is a any other existing revision and throw exception if its there
				if (anyExistingRevision != null) { // Looks like still some active version exist
					log.warn("Application couldn't be undeployed :: " + anyExistingRevision);
					deployment1.state = STATE_ERROR;
				}
			}
		}

		return deployment1.state;

	}

	public String refreshBundle(Bundle bundle) throws IOException {

		try {
			// FIXME why are we passing strings around to figure out if a method call was successful or not?
			// Throw an error and handle that one
			String state = deactivateBundle(bundle); // de-activating the existing
			// bundle
			if (!state.equals(STATE_UNDEPLOYED)) {
				throw new IOException("The bundle is not undeployed");
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new IOException("Error in undeploying bundle");
		}

		log.info("Activating Version: " + bundle.getRevision() + " For Env Profile: " + profile.getEnvironment());
		return activateBundleRevision(bundle);

	}

	// This function should do -
	// Return a revision if there is a active revision, if there are more than one active revisions deployed in an env, the highest revision number is picked
	// Returns "" if there are no active revision


	public String activateBundleRevision(Bundle bundle) throws IOException {

		BundleActivationConfig deployment2 = new BundleActivationConfig();

		try {

			HttpHeaders headers = new HttpHeaders();
			headers.setAccept("application/json");

			GenericUrl url = new GenericUrl(format("%s/%s/organizations/%s/environments/%s/%s/%s/revisions/%d/deployments",
					profile.getHostUrl(),
					profile.getApi_version(),
					profile.getOrg(),
					profile.getEnvironment(),
					bundle.getType().getPathName(),
					bundle.getName(),
					bundle.getRevision()));

			GenericData data = new GenericData();
			data.set("override", Boolean.valueOf(getProfile().isOverride()).toString());

			//Fix for https://github.com/apigee/apigee-deploy-maven-plugin/issues/18
			if (profile.getDelayOverride() != 0) {
				data.set("delay", profile.getDelayOverride());
			}

			HttpRequest deployRestRequest = requestFactory.buildPostRequest(url, new UrlEncodedContent(data));
			deployRestRequest.setReadTimeout(0);
			deployRestRequest.setHeaders(headers);


			HttpResponse response = null;
			response = executeAPI(profile, deployRestRequest);
			String responseString = response.parseAsString();
			SeamLessDeploymentStatus deployment3 = null;
            try{
            	deployment3 = new Gson().fromJson(responseString, SeamLessDeploymentStatus.class);
            }catch (JsonSyntaxException e){
            	// https://github.com/apigee/apigee-deploy-maven-plugin/issues/92
            	// https://github.com/apigee/apigee-deploy-maven-plugin/issues/137
            	// Whenever an existing API is deployed with option as override and in the new revision, the proxy basepath is changed,
            	// the Mgmt API response is different. It does not return the usual response if the proxy has no changes to the basepath
            	deployment2 = new Gson().fromJson(responseString, BundleActivationConfig.class);
            	if (log.isInfoEnabled()) {
    				log.info(PrintUtil.formatResponse(response, gson.toJson(deployment2)));
    				log.info("Deployed revision is:{}", deployment2.revision);
    			}
    			applyDelay();
    			return deployment2.state;
            }

			if (getProfile().isOverride()) {
				//deployment3 = response.parseAs(SeamLessDeploymentStatus.class);
				Iterator<BundleActivationConfig> iter = deployment3.environment.iterator();
				while (iter.hasNext()) {
					BundleActivationConfig config = iter.next();
					if (config.environment.equalsIgnoreCase(profile.getEnvironment())) {
						if (!config.state.equalsIgnoreCase("deployed")) {
							log.info("Waiting to assert bundle activation.....");
							Thread.sleep(10);
							Long deployedRevision = getDeployedRevision(bundle);
							if (bundle.getRevision() != null && bundle.getRevision().equals(deployedRevision)) {
								log.info("Deployed revision is: " + bundle.getRevision());
								return "deployed";
							} else
								log.error("Deployment failed to activate");
							throw new MojoExecutionException("Deployment failed: Bundle did not activate within expected time. Please check deployment status manually before trying again");
						} else {
							log.info(PrintUtil.formatResponse(response, gson.toJson(deployment3)));
						}
					}
				}

			}

			deployment2 = new Gson().fromJson(responseString, BundleActivationConfig.class);
			if (log.isInfoEnabled()) {
				log.info(PrintUtil.formatResponse(response, gson.toJson(deployment2)));
				log.info("Deployed revision is:{}", deployment2.revision);
			}
			applyDelay();

		} catch (Exception e) {
			log.error(e.getMessage());
			throw new IOException(e);
		}

		return deployment2.state;

	}

	public Long deleteBundle(Bundle bundle) throws IOException {
		Long deployedRevision = getDeployedRevision(bundle);

		if (deployedRevision == bundle.getRevision()) { // the same version is the active bundle deactivate first
			deactivateBundle(bundle);
		}

		GenericUrl url = new GenericUrl(format("%s/%s/organizations/%s/%s/%s/revisions/%d",
				profile.getHostUrl(),
				profile.getApi_version(),
				profile.getOrg(),
				bundle.getType().getPathName(),
				bundle.getName(),
				bundle.getRevision()));

		HttpHeaders headers = new HttpHeaders();
		headers.setAccept("application/json");
		headers.setContentType("application/octet-stream");
		HttpRequest deleteRestRequest = requestFactory.buildDeleteRequest(url);
		deleteRestRequest.setReadTimeout(0);
		deleteRestRequest.setHeaders(headers);

		HttpResponse response = null;
		response = executeAPI(profile, deleteRestRequest);

		//		String deleteResponse = response.parseAsString();
		AppConfig deleteResponse = response.parseAs(AppConfig.class);
		if (log.isInfoEnabled())
			log.info(PrintUtil.formatResponse(response, gson.toJson(deleteResponse).toString()));
		applyDelay();
		return deleteResponse.getRevision();
	}

	public static class AppRevision {

		@Key
		private String name;

		@Key
		private List<String> revision;

		public String getName() {
			return name;
		}

		public List<Long> getRevision() {
			if (revision != null) {
				List<Long> revisions = new ArrayList<Long>(revision.size());
				for (String r : revision) {
					revisions.add(Long.parseLong(r));
				}
				return revisions;
			} else {
				return null;
			}
		}

	}

	public static class ConfigVersion {

		@Key
		public int majorVersion;

		@Key
		public int minorVersion;
	}

	public static class Server {

		@Key
		public String status;
		@Key
		public List<String> type;
		@Key
		public String uUID;
	}

	public static class Configuration {

		@Key
		public String basePath;
		@Key
		public List<String> steps;
	}

	public static class Revision {

		@Key
		public Configuration configuration;
		@Key
		public String name;
		@Key
		public List<Server> server;
		@Key
		public String state;
	}

	public static class Environment {

		@Key
		public String name;
		@Key
		public List<Revision> revision;
	}

	public static class BundleDeploymentConfig {

		@Key
		public List<Environment> environment;
		@Key
		public String name;
		@Key
		public String organization;
	}

	public static class BundleActivationConfig {

		@Key
		public String aPIProxy;
		@Key
		public String sharedFlow;
		@Key
		public Configuration configuration;
		@Key
		public String environment;
		@Key
		public String name;
		@Key
		public String organization;
		@Key
		public String revision;
		@Key
		public String state;

		@Key
		public List<Server> server;

	}

	public static class SeamLessDeploymentStatus {

		@Key
		public String aPIProxy;
		@Key
		public String sharedFlow;
		@Key
		public List<BundleActivationConfig> environment;
		@Key
		public String organization;
	}

	public static class AppConfig {

		@Key
		public ConfigVersion configurationVersion;

		@Key
		public String contextInfo;

		@Key
		public long createdAt;

		@Key
		public String createdBy;

		@Key
		public long lastModifiedAt;

		@Key
		public String lastModifiedBy;

		@Key
		public List<String> policies;

		@Key
		public List<String> proxyEndpoints;

		@Key
		public List<String> resources;
		@Key
		public List<String> targetEndpoints;
		@Key
		public List<String> targetServers;
		@Key
		public String type;
		@Key
		private String revision;

		public Long getRevision() {
			return Long.parseLong(revision);
		}

	}
}
