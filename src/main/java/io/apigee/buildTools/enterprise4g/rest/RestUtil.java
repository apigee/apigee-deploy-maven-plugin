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
package io.apigee.buildTools.enterprise4g.rest;

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
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.client.util.GenericData;
import com.google.api.client.util.Key;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import io.apigee.buildTools.enterprise4g.utils.PrintUtil;
import io.apigee.buildTools.enterprise4g.utils.ServerProfile;
import io.apigee.buildTools.enterprise4g.utils.StringToIntComparator;

public class RestUtil {

    static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
    static final JsonFactory JSON_FACTORY = new JacksonFactory();
    static String versionRevision;
    static Logger logger = LoggerFactory.getLogger(RestUtil.class);
    static Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static final String STATE_UNDEPLOYED = "undeployed";
    public static final String STATE_DEPLOYED = "deployed";
    public static final String STATE_ERROR = "error";
    public static final String STATE_IMPORTED = "imported";

    static String accessToken = null;
    //static final String mgmtAPIClientId = "edgecli";
    //static final String mgmtAPIClientSecret = "edgeclisecret";

    public static class Options {

        public static boolean force;
        public static boolean clean;
        public static boolean update;
        public static boolean inactive;
        public static boolean override;
        public static boolean validate;
        public static long delay;
        public static long override_delay;

    }


    public static class AppRevision {
        @Key
        public String name;

        @Key
        public List<String> revision;
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
        public String revision;

        @Key
        public List<String> targetEndpoints;

        @Key
        public List<String> targetServers;

        @Key
        public String type;

        public String getRevision() {
            return revision;
        }

        public void setRevision(String revision) {
            this.revision = revision;
        }
    }


    static HttpRequestFactory REQUEST_FACTORY = HTTP_TRANSPORT
            .createRequestFactory(new HttpRequestInitializer() {
                // @Override
                public void initialize(HttpRequest request) {
                    request.setParser(JSON_FACTORY.createJsonObjectParser());
                    XTrustProvider.install();
                    FakeHostnameVerifier _hostnameVerifier = new FakeHostnameVerifier();
                    // Install the all-trusting host name verifier:
                    HttpsURLConnection.setDefaultHostnameVerifier(_hostnameVerifier);

                }
            });

    public static void initMfa(ServerProfile profile) throws IOException {

    	// any simple get request can be used to - we just need to get an access token
    	// whilst the mfatoken is still valid
    	
        // trying to construct the URL like
        // https://api.enterprise.apigee.com/v1/organizations/apigee-cs/apis/taskservice/
        // success response is ignored
    	if (accessToken == null) {
			logger.info("=============Initialising MFA================");
	
	        HttpRequest restRequest = REQUEST_FACTORY
	                .buildGetRequest(new GenericUrl(profile.getHostUrl() + "/"
	                        + profile.getApi_version() + "/organizations/"
	                        + profile.getOrg() + "/apis/"
	                        + profile.getApplication() + "/"));
	        restRequest.setReadTimeout(0);
	        HttpHeaders headers = new HttpHeaders();
	        headers.setAccept("application/json");
	        restRequest.setHeaders(headers);
	
	        try {
	            HttpResponse response = executeAPI(profile, restRequest);            
	            //ignore response - we just wanted the MFA initialised
	            logger.info("=============MFA Initialised================");
	        } catch (HttpResponseException e) {
	            logger.error(e.getMessage());
	            //throw error as there is no point in continuing
	            throw e;
	        }
    	}
    }

    public static void getRevision(ServerProfile profile) throws IOException {

        // trying to construct the URL like
        // https://api.enterprise.apigee.com/v1/organizations/apigee-cs/apis/taskservice/
        // response is like
        // {
        // "name" : "taskservice1",
        // "revision" : [ "1" ]
        // }
        HttpRequest restRequest = REQUEST_FACTORY
                .buildGetRequest(new GenericUrl(profile.getHostUrl() + "/"
                        + profile.getApi_version() + "/organizations/"
                        + profile.getOrg() + "/apis/"
                        + profile.getApplication() + "/"));
        restRequest.setReadTimeout(0);
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept("application/json");
        restRequest.setHeaders(headers);

        try {
            HttpResponse response = executeAPI(profile, restRequest);
            AppRevision apprev = response.parseAs(AppRevision.class);
            Collections.sort(apprev.revision, new StringToIntComparator());
            setVersionRevision(apprev.revision.get(0));
            logger.info(PrintUtil.formatResponse(response, gson.toJson(apprev).toString()));
        } catch (HttpResponseException e) {
            logger.error(e.getMessage());
        }
    }
    
    public static String getLatestRevision(ServerProfile profile) throws IOException {

        // trying to construct the URL like
        // https://api.enterprise.apigee.com/v1/organizations/apigee-cs/apis/taskservice/
        // response is like
        // {
        // "name" : "taskservice1",
        // "revision" : [ "1" ]
        // }
    	String revision = "";
        HttpRequest restRequest = REQUEST_FACTORY
                .buildGetRequest(new GenericUrl(profile.getHostUrl() + "/"
                        + profile.getApi_version() + "/organizations/"
                        + profile.getOrg() + "/apis/"
                        + profile.getApplication() + "/"));
        restRequest.setReadTimeout(0);
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept("application/json");
        restRequest.setHeaders(headers);

        try {
            HttpResponse response = executeAPI(profile, restRequest);
            AppRevision apprev = response.parseAs(AppRevision.class);
            Collections.sort(apprev.revision, new StringToIntComparator());
            revision = apprev.revision.get(0);
            logger.info(PrintUtil.formatResponse(response, gson.toJson(apprev).toString()));
        } catch (HttpResponseException e) {
            logger.error(e.getMessage());
        }
        return revision;
    }

    // This function should do -
    // Return a revision if there is a active revision, if there are more than one active revisions deployed in an env, the highest revision number is picked
    // Returns "" if there are no active revision

    public static String getDeployedRevision(ServerProfile profile)
            throws IOException {
    	if(profile.getApi_type()!=null && profile.getApi_type().equalsIgnoreCase("sharedflow")){
    		return getDeployedRevision(profile, "sharedflows");
    	}	
    	else{
    		return getDeployedRevision(profile, "apis");
    	}
    		
    }
    
    public static String getDeployedRevision(ServerProfile profile, String type)
            throws IOException {

        BundleDeploymentConfig deployment1 = null;

        try {

            HttpRequest restRequest = REQUEST_FACTORY
                    .buildGetRequest(new GenericUrl(profile.getHostUrl() + "/"
                            + profile.getApi_version() + "/organizations/"
                            + profile.getOrg() + "/"+type+"/"
                            + profile.getApplication() + "/deployments"));
            restRequest.setReadTimeout(0);
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept("application/json");
            restRequest.setHeaders(headers);

            HttpResponse response = executeAPI(profile, restRequest);
            deployment1 = response.parseAs(BundleDeploymentConfig.class);
            logger.debug(PrintUtil.formatResponse(response, gson.toJson(deployment1).toString()));


            /*if (deployment1 != null) {
                for (Environment env : deployment1.environment) {
                    if (env.name.equalsIgnoreCase(profile.getEnvironment()))
                        return env.revision.get(0).name;
                }
            }*/
            //Fix for https://github.com/apigee/apigee-deploy-maven-plugin/issues/53
            if (deployment1 != null) {
                for (Environment env : deployment1.environment) {
                    if (env.name.equalsIgnoreCase(profile.getEnvironment())){
                    	List<Revision> revisionList = env.revision;
                    	if(revisionList!=null && revisionList.size()>0){
                    		List<String> revisions = new ArrayList<String>();
                    		for (Revision revision : revisionList) {
                    			revisions.add(revision.name);
							}
                    		Collections.sort(revisions, new StringToIntComparator());
                    		return revisions.get(0);
                    	}
                    }
                }
            }

        } catch (HttpResponseException e) {
            logger.error(e.getMessage());
        } catch (Exception e) {
            logger.error(e.getMessage());
        }

        //This is not correct , it will always return the 1st env's deployed revision .
        //return deployment1.environment.get(0).revision.get(0).name;
        return "";


    }

    public static String uploadBundle(ServerProfile profile, String bundleFile)
            throws IOException {
    	if(profile.getApi_type()!=null && profile.getApi_type().equalsIgnoreCase("sharedflow")){
    		return uploadBundle(profile, bundleFile, "sharedflows");
    	}	
    	else{
    		return uploadBundle(profile, bundleFile, "apis");
    	}
    }

    public static String uploadBundle(ServerProfile profile, String bundleFile, String type)
            throws IOException {

        FileContent fContent = new FileContent("application/octet-stream",
                new File(bundleFile));
        //testing
        logger.debug("URL parameters API Version{}", (profile.getApi_version()));
        logger.debug("URL parameters URL {}", (profile.getHostUrl()));
        logger.debug("URL parameters Org{}", (profile.getOrg()));
        logger.debug("URL parameters App {}", (profile.getApplication()));

        //Forcefully validate before deployment
        String importCmd = profile.getHostUrl() + "/"
                + profile.getApi_version() + "/organizations/"
                + profile.getOrg() + "/"+type+"?action=import&name="
                + profile.getApplication();
        if (Options.validate) {
            importCmd = importCmd + "&validate=true";
        }

        HttpRequest restRequest = REQUEST_FACTORY.buildPostRequest(
                new GenericUrl(importCmd), fContent);
        restRequest.setReadTimeout(0);
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept("application/json");
        restRequest.setHeaders(headers);

        try {
            HttpResponse response = executeAPI(profile, restRequest);


            // logger.info(response.parseAsString());
            AppConfig appconf = response.parseAs(AppConfig.class);
            setVersionRevision(appconf.revision);
            logger.info(PrintUtil.formatResponse(response, gson.toJson(appconf).toString()));

            //Introduce Delay
            if (Options.delay != 0) {
                try {
                    logger.info("Delay of " + Options.delay + " milli second");
                    Thread.sleep(Options.delay);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        } catch (HttpResponseException e) {
            logger.error(e.getMessage());
            throw new IOException(e.getMessage());
        }

        return getVersionRevision();

    }

    public static String updateBundle(ServerProfile profile, String bundleFile, String revision)
            throws IOException {
    	if(profile.getApi_type()!=null && profile.getApi_type().equalsIgnoreCase("sharedflow")){
    		return updateBundle(profile, bundleFile, revision, "sharedflows");
    	}	
    	else{
    		return updateBundle(profile, bundleFile, revision, "apis");
    	}
    }

    public static String updateBundle(ServerProfile profile, String bundleFile, String revision, String type)
            throws IOException {

        FileContent fContent = new FileContent("application/octet-stream",
                new File(bundleFile));
        //System.out.println("\n\n\nFile path: "+ new File(bundleFile).getCanonicalPath().toString());
        logger.debug("URL parameters API Version{}", (profile.getApi_version()));
        logger.debug("URL parameters URL {}", (profile.getHostUrl()));
        logger.debug("URL parameters Org{}", (profile.getOrg()));
        logger.debug("URL parameters App {}", (profile.getApplication()));

        //Forcefully validate before deployment
        String importCmd = profile.getHostUrl() + "/"
                + profile.getApi_version() + "/organizations/"
                + profile.getOrg() + "/"+type+"/"
                + profile.getApplication() + "/revisions/"
                + revision+"?validate=true";

        if (Options.validate) {
            importCmd = importCmd + "&validate=true";
        }

        HttpRequest restRequest = REQUEST_FACTORY.buildPostRequest(
                new GenericUrl(importCmd), fContent);
        restRequest.setReadTimeout(0);
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept("application/json");
        restRequest.setHeaders(headers);

        try {
            HttpResponse response = executeAPI(profile, restRequest);
            AppConfig appconf = response.parseAs(AppConfig.class);
            setVersionRevision(appconf.revision);
            logger.info(PrintUtil.formatResponse(response, gson.toJson(appconf).toString()));

            //Introduce Delay
            if (Options.delay != 0) {
                try {
                    logger.info("Delay of " + Options.delay + " milli second");
                    Thread.sleep(Options.delay);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        } catch (HttpResponseException e) {
            logger.error(e.getMessage());
            throw new IOException(e.getMessage());
        }

        return getVersionRevision();

    }

    public static String deactivateBundle(ServerProfile profile)
            throws IOException {
    	if(profile.getApi_type()!=null && profile.getApi_type().equalsIgnoreCase("sharedflow")){
    		return deactivateBundle(profile, "sharedflows");
    	}	
    	else{
    		return deactivateBundle(profile, "apis");
    	}
    }

    public static String deactivateBundle(ServerProfile profile, String type)
            throws IOException {
        String existingRevision = "";
        BundleActivationConfig deployment1 = new BundleActivationConfig();
        try {

            existingRevision = getDeployedRevision(profile);

            if (existingRevision.length() > 0) //  there are active revisions
            // deployment then undeploy
            {

                logger.info("De-activating Version: " + existingRevision + " For Env Profile: " + profile.getEnvironment());
                String undeployCmd = profile.getHostUrl() + "/"
                        + profile.getApi_version() + "/organizations/"
                        + profile.getOrg() + "/environments/"
                        + profile.getEnvironment() + "/"+type+"/"
                        + profile.getApplication() + "/revisions/"
                        + existingRevision
                        + "/deployments";


                HttpRequest undeployRestRequest = REQUEST_FACTORY.buildDeleteRequest(
                        new GenericUrl(undeployCmd));
                undeployRestRequest.setReadTimeout(0);
                HttpHeaders headers = new HttpHeaders();
                headers.setAccept("application/json");
                undeployRestRequest.setHeaders(headers);

                HttpResponse response = null;
                response = executeAPI(profile, undeployRestRequest);
                deployment1 = response.parseAs(BundleActivationConfig.class);
                logger.info(PrintUtil.formatResponse(response, gson.toJson(deployment1).toString()));

                //Introduce Delay
                if (Options.delay != 0) {
                    try {
                        logger.info("Delay of " + Options.delay + " milli second");
                        Thread.sleep(Options.delay);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            } else {
                //If there are no existing active revisions
                deployment1.state = STATE_UNDEPLOYED;
            }

        } catch (HttpResponseException e) {
            logger.error(e.getMessage());
            //deployment1.state = "No application was in deployed state";
            deployment1.state = STATE_ERROR;

        } catch (Exception e) {
            logger.error(e.getMessage());
            //deployment1.state = "No application was in deployed state";
            deployment1.state = STATE_ERROR;
        } finally {


            //Rechecking only if we supply force option.  Checking if still any active revision exists
            if (Options.force) {

                String anyExistingRevision = "";

                try {
                    logger.info("Checking if any deployed version still exists for Env Profile: " + profile.getEnvironment());
                    anyExistingRevision = getDeployedRevision(profile);

                } catch (Exception e) {
                    //deployment1.state = "\nNo application is in deployed state\n";
                    logger.error("Application couldn't be undeployed :: " + anyExistingRevision);
                    deployment1.state = STATE_ERROR;
                }
                //check  if there is a any other existing revision and throw exception if its there
                if (anyExistingRevision.length() > 0) // Looks like still some active version exist
                {
                    logger.warn("Application couldn't be undeployed :: " + anyExistingRevision);
                    deployment1.state = STATE_ERROR;
                }

            }


        }

        return deployment1.state;

    }


    public static String refreshBundle(ServerProfile profile, String revision)
            throws IOException {

        String state = "";
        try {

            state = deactivateBundle(profile); // de-activating the existing
            // bundle
            if (!state.equals(STATE_UNDEPLOYED)) {
                throw new IOException("The bundle is not undeployed");
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw new IOException("Error in undeploying bundle");
        }

        logger.info("Activating Version: " + revision + " For Env Profile: " + profile.getEnvironment());
        return activateBundleRevision(profile, revision);

    }

    
    public static String activateBundleRevision(ServerProfile profile, String revision)
            throws IOException {
    	if(profile.getApi_type()!=null && profile.getApi_type().equalsIgnoreCase("sharedflow")){
    		return activateBundleRevision(profile, revision, "sharedflows");
    	}	
    	else{
    		return activateBundleRevision(profile, revision, "apis");
    	}
    }
    
    public static String activateBundleRevision(ServerProfile profile, String revision, String type)
            throws IOException {

        BundleActivationConfig deployment2 = new BundleActivationConfig();
        HttpResponse response = null;
        try {

            UrlEncodedContent urlEncodedContent = null;

            HttpHeaders headers = new HttpHeaders();
            headers.setAccept("application/json");
            //headers.setContentType("application/x-www-form-urlencoded");

            String deployCmd = profile.getHostUrl() + "/"
                    + profile.getApi_version() + "/organizations/"
                    + profile.getOrg() + "/environments/"
                    + profile.getEnvironment() + "/"+type+"/"
                    + profile.getApplication() + "/revisions/" + revision
                    + "/deployments";

            if (Options.override) {
                GenericData data = new GenericData();
                data.set("override", "true");
                //Fix for https://github.com/apigee/apigee-deploy-maven-plugin/issues/18
                if (Options.override_delay != 0) {
                    data.set("delay", Options.override_delay);
                }
                urlEncodedContent = new UrlEncodedContent(data);
            } else {
                // https://github.com/apigee/apigee-deploy-maven-plugin/issues/56
                GenericData data = new GenericData();
                data.set("override", "false");
                urlEncodedContent = new UrlEncodedContent(data);
            }


            HttpRequest deployRestRequest = REQUEST_FACTORY.buildPostRequest(
                    new GenericUrl(deployCmd), urlEncodedContent);
            deployRestRequest.setReadTimeout(0);
            deployRestRequest.setHeaders(headers);

            response = executeAPI(profile, deployRestRequest);
            String responseString = response.parseAsString();
            SeamLessDeploymentStatus deployment3 = null;
            try{
            	deployment3 = new Gson().fromJson(responseString, SeamLessDeploymentStatus.class);
            }catch (JsonSyntaxException e){
            	// https://github.com/apigee/apigee-deploy-maven-plugin/issues/92
            	// Whenever an existing API is deployed with option as override and in the new revision, the proxy basepath is changed,
            	// the Mgmt API response is different. It does not return the usual response if the proxy has no changes to the basepath
            	// So catching the exception from above and setting the override flag to false so that it doesnt go that section of code below
            	Options.override = false;
            }
            if (Options.override) {
                //SeamLessDeploymentStatus deployment3 = response.parseAs(SeamLessDeploymentStatus.class);
                Iterator<BundleActivationConfig> iter =   deployment3.environment.iterator();
                while (iter.hasNext()){
                    BundleActivationConfig config = iter.next();
                    if (config.environment.equalsIgnoreCase(profile.getEnvironment())) {
                        if (!config.state.equalsIgnoreCase("deployed"))
                         {
                             logger.info("\nWaiting to assert bundle activation.....");
                             Thread.sleep(10);
                             if (getDeployedRevision(profile).equalsIgnoreCase(revision))
                             {
                            	 logger.info("\nDeployed revision is: " + revision);
                                 return "deployed";
                             }
                             else
                                 logger.error("Deployment failed to activate");
                                 throw new MojoExecutionException("Deployment failed: Bundle did not activate within expected time. Please check deployment status manually before trying again");
                         }
                        else {
                            logger.info(PrintUtil.formatResponse(response, gson.toJson(deployment3).toString()));
                        }
                    }
                }

            }
            //deployment2 = response.parseAs(BundleActivationConfig.class);
            deployment2 = new Gson().fromJson(responseString, BundleActivationConfig.class);
            logger.info(PrintUtil.formatResponse(response, gson.toJson(deployment2).toString()));
            logger.info("\nDeployed revision is: "+deployment2.revision);

            //Introduce Delay
            if (Options.delay != 0) {
                try {
                    logger.debug("Delay of " + Options.delay + " milli second");
                    Thread.sleep(Options.delay);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }


        } catch (Exception e) {
            logger.error(e.getMessage());
            throw new IOException(e);
        }

        return deployment2.state;

    }


    public static String deleteBundle(ServerProfile profile, String revision)
            throws IOException {
        // get the deployed revision
        String deployed_revision = "";

        try {
            deployed_revision = getDeployedRevision(profile);
        } catch (Exception e) {
            throw new IOException("Error fetching deployed revision");
        }


        if (deployed_revision.equals(revision)) // the same version is the active bundle deactivate first
        {
            deactivateBundle(profile);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept("application/json");
        headers.setContentType("application/octet-stream");
        HttpRequest deleteRestRequest = REQUEST_FACTORY.buildDeleteRequest(
                new GenericUrl(profile.getHostUrl() + "/"
                        + profile.getApi_version() + "/organizations/"
                        + profile.getOrg() + "/apis/"
                        + profile.getApplication() + "/revisions/" + revision));
        deleteRestRequest.setReadTimeout(0);
        deleteRestRequest.setHeaders(headers);

        HttpResponse response = null;
        response = executeAPI(profile, deleteRestRequest);

        //		String deleteResponse = response.parseAsString();
        AppConfig deleteResponse = response.parseAs(AppConfig.class);
        logger.info(PrintUtil.formatResponse(response, gson.toJson(deleteResponse).toString()));

        //Introduce Delay
        if (Options.delay != 0) {
            try {
                logger.info("Delay of " + Options.delay + " milli second");
                Thread.sleep(Options.delay);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        return deleteResponse.getRevision();

    }

    public static String getVersionRevision() {
        return versionRevision;
    }

    public static void setVersionRevision(String versionRevision) {
        RestUtil.versionRevision = versionRevision;
    }

    /**
     * OAuth token acquisition for calling management APIs
     * Access Token expiry 1799 sec = 30 mins long enough to finish any maven task
     * MFA Token: TOTP expires in 30 secs. User needs to give a token with some validity
     */
    private static HttpResponse executeAPI(ServerProfile profile, HttpRequest request) 
            throws IOException {
        HttpHeaders headers = request.getHeaders();
        MgmtAPIClient client = new MgmtAPIClient();
        String mfaToken = profile.getMFAToken();
        String tokenUrl = profile.getTokenUrl();
        String mgmtAPIClientId = (profile.getClientId()!=null && !profile.getClientId().equalsIgnoreCase(""))?profile.getClientId():"edgecli";
        String mgmtAPIClientSecret = (profile.getClientSecret()!=null && !profile.getClientSecret().equalsIgnoreCase(""))?profile.getClientSecret():"edgeclisecret";
        /**** Basic Auth - Backward compatibility ****/
        if (profile.getAuthType() != null &&
            profile.getAuthType().equalsIgnoreCase("basic")) {
                headers.setBasicAuthentication(profile.getCredential_user(),
                                                profile.getCredential_pwd());
                logger.info(PrintUtil.formatRequest(request));
                return request.execute();
        }

        /**** OAuth ****/
        if (profile.getBearerToken() != null && !profile.getBearerToken().equalsIgnoreCase("")){
        	//Need to validate access token only if refresh token is provided. 
	        	//If access token is not valid, create a bearer token using the refresh token 
	        	//If access token is valid, use that 
        	accessToken = (accessToken!=null)?accessToken:profile.getBearerToken();        	
        	if(profile.getRefreshToken() != null && !profile.getRefreshToken().equalsIgnoreCase("")){
        		if(isValidBearerToken(accessToken, profile, mgmtAPIClientId)){
        			logger.info("Access Token valid");
        			headers.setAuthorization("Bearer " + accessToken);
                 }else{
                	 try{
                		 AccessToken token = null;
                		 logger.info("Access token not valid so acquiring new access token using Refresh Token");
                		 token = client.getAccessTokenFromRefreshToken(
		     			 			tokenUrl,
		     			 			mgmtAPIClientId, mgmtAPIClientSecret, 
		     			 			profile.getRefreshToken());
                		 logger.info("New Access Token acquired");
			         	 accessToken = token.getAccess_token();
			             headers.setAuthorization("Bearer " + accessToken);
                	 }catch (Exception e) {
                        logger.error(e.getMessage());
                        throw new IOException(e.getMessage());
                     }
                 }
        	}
        	//if refresh token is not passed, validate the access token and use it accordingly
        	else{
        		logger.info("Validating the access token passed");
        		if(isValidBearerToken(profile.getBearerToken(), profile, mgmtAPIClientId)){
        			logger.info("Access Token valid");
        			accessToken = profile.getBearerToken();
                    headers.setAuthorization("Bearer " + accessToken);
        		}else{
        			logger.error("Access token not valid");
        			throw new IOException ("Access token not valid");
        		}
        		
        	}
        }
        else if (accessToken != null) {
            // subsequent calls
            logger.debug("Reusing mgmt API access token");
            headers.setAuthorization("Bearer " + accessToken);
        } else {
            logger.info("Acquiring mgmt API token from " + tokenUrl);
            try {
                AccessToken token = null;
                if (mfaToken == null || mfaToken.length() == 0) {
                    logger.info("MFA token not provided. Skipping.");
                    token = client.getAccessToken(
                            tokenUrl,
                            mgmtAPIClientId, mgmtAPIClientSecret,
                            profile.getCredential_user(),
                            profile.getCredential_pwd());
                } else {
                    logger.info("Making use of the MFA token provided.");
                    token = client.getAccessToken(
                            tokenUrl,
                            mgmtAPIClientId, mgmtAPIClientSecret,
                            profile.getCredential_user(),
                            profile.getCredential_pwd(),
                            profile.getMFAToken());
                }
                accessToken = token.getAccess_token();
                headers.setAuthorization("Bearer " + accessToken);
            } catch (Exception e) {
                logger.error(e.getMessage());
                // should we throw something up ??
            }
        }
        logger.info(PrintUtil.formatRequest(request));
        return request.execute();
    }
    
    
    /**
     * This method is used to validate the Bearer token. It validates the source and the expiration and if the token is about to expire in 30 seconds, set as invalid token
     * @param accessToken
     * @param profile
     * @param clientId
     * @return
     * @throws IOException
     */
    private static boolean isValidBearerToken(String accessToken, ServerProfile profile, String clientId) throws IOException{
    	boolean isValid = false;
    	try {
		    JWT jwt = JWT.decode(accessToken);
		    String jwtClientId = jwt.getClaim("client_id").asString();
		    String jwtEmailId = jwt.getClaim("email").asString();
		    long jwtExpiresAt = jwt.getExpiresAt().getTime()/1000;
		    long difference = jwtExpiresAt - (System.currentTimeMillis()/1000);
		    if(jwt!= null && jwtClientId!=null && jwtClientId.equals(clientId)
	    		&& jwtEmailId!=null && jwtEmailId.equalsIgnoreCase(profile.getCredential_user())
	    		&& profile.getTokenUrl().contains(jwt.getIssuer())
	    		&& difference >= 30){
		    	isValid = true;
		    }
		} catch (JWTDecodeException exception){
		   throw new IOException(exception.getMessage());
		}
    	return isValid;
    }
}
