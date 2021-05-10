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
import com.google.api.client.http.FileContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpMediaType;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.MultipartContent;
import com.google.api.client.http.UrlEncodedContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
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
        public static boolean async;
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

    public static class Deployment {
    	@Key
        public String environment;
    	@Key
        public String apiProxy;
        @Key
        public String revision;
        @Key
        public String deployStartTime;
        @Key
        public String basePath;
    }
    
    public static class BundleDeploymentConfig {
        @Key
        public List<Deployment> deployments;
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

    public static class DeploymentStatus {
        @Key
        public String apiProxy;
        @Key
        public String sharedFlow;
        @Key
        public String environment;
        @Key
        public String revision;
        @Key
        public String deployStartTime;
        @Key
        public String basePath;
        @Key
        public String state;
        @Key
        public List<PodStatus> pods;
        @Key
        public String organization;
    }

    public static class PodStatus {
    	@Key
        public String podName;
    	@Key
        public String appVersion;
    	@Key
        public String podStatus;
    	@Key
        public String podStatusTime;
    	@Key
        public String deploymentStatusTime;
    	@Key
        public String deploymentTime;
    	@Key
        public String deploymentStatus;
    	@Key
        public String statusCode;
    	@Key
        public String statusCodeDetails;
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
    
    public static boolean getDeploymentStateForRevision(ServerProfile profile, String revision)
            throws IOException {
    	if(profile.getApi_type()!=null && profile.getApi_type().equalsIgnoreCase("sharedflow")){
    		return getDeploymentStateForRevision(profile, "sharedflows", revision);
    	}	
    	else{
    		return getDeploymentStateForRevision(profile, "apis", revision);
    	}
    		
    }
    public static boolean getDeploymentStateForRevision(ServerProfile profile, String type, String revision)
            throws IOException {
    	DeploymentStatus deploymentStatus = null;
    	boolean deployed = false;
    	try {
    		logger.info("Getting Deployment Info for Revision: "+ revision);
    		HttpRequest restRequest = REQUEST_FACTORY
                    .buildGetRequest(new GenericUrl(profile.getHostUrl() + "/"
                            + profile.getApi_version() + "/organizations/"
                            + profile.getOrg() + "/environments/"
                            + profile.getEnvironment() + "/"+type+"/"
                            + profile.getApplication() + "/revisions/" + revision
                            + "/deployments"));
    		restRequest.setReadTimeout(0);
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept("application/json");
            restRequest.setHeaders(headers);
            HttpResponse response = executeAPI(profile, restRequest);
            deploymentStatus = response.parseAs(DeploymentStatus.class);
            /*if(deploymentStatus!=null && deploymentStatus.pods!=null && deploymentStatus.pods.size()>0) {
            	for (PodStatus p: deploymentStatus.pods) {
            		if(p.deploymentStatus!=null && p.deploymentStatus.equals("deployed")) {
						deployed = true;
					}else {
						deployed = false;
						break;
					}
						
				}
            }*/
            if(deploymentStatus!=null && deploymentStatus.state != null && deploymentStatus.state.equalsIgnoreCase("READY")) {
            	deployed = true;
            }else {
            	deployed = false;
            }
    	}catch (HttpResponseException e) {
            logger.error(e.getMessage());
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    	logger.debug("returning deployed status: "+ deployed);
    	return deployed;
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

        BundleDeploymentConfig bundleDeploymentConfig = null;

        try {
            HttpRequest restRequest = REQUEST_FACTORY
                    .buildGetRequest(new GenericUrl(profile.getHostUrl() + "/"
                            + profile.getApi_version() + "/organizations/"
                            + profile.getOrg() + "/environments/"+profile.getEnvironment()+"/"
                            + type+"/"+ profile.getApplication() + "/deployments"));
            restRequest.setReadTimeout(0);
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept("application/json");
            restRequest.setHeaders(headers);

            HttpResponse response = executeAPI(profile, restRequest);
            bundleDeploymentConfig = response.parseAs(BundleDeploymentConfig.class);
            logger.debug(PrintUtil.formatResponse(response, gson.toJson(bundleDeploymentConfig).toString()));
            if (bundleDeploymentConfig != null && bundleDeploymentConfig.deployments !=null && bundleDeploymentConfig.deployments.size()>0) {
            	for (Deployment deployment : bundleDeploymentConfig.deployments) {
					if(deployment.environment.equalsIgnoreCase(profile.getEnvironment())) {
						logger.info("Deployed revision: "+deployment.revision);
						return deployment.revision;
					}
				}
            }
        } catch (HttpResponseException e) {
            logger.error(e.getMessage());
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
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

    	MultipartContent content = new MultipartContent().setMediaType(
    	        new HttpMediaType("multipart/form-data")
    	                .setParameter("boundary", "__END_OF_PART__"));
    		 
    	FileContent fContent = new FileContent("application/octet-stream", new File(bundleFile));
    	MultipartContent.Part part = new MultipartContent.Part(fContent);
    	part.setHeaders(new HttpHeaders().set(
    	        "Content-Disposition", 
    	        String.format("form-data; name=\"content\"; file=\"%s\"", new File(bundleFile).getName())));
    	content.addPart(part);
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
                new GenericUrl(importCmd), content);
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
            	logger.info("No active revisions to deactivate");
                return null;
            }

        } catch (HttpResponseException e) {
            logger.error(e.getMessage());          

        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return STATE_UNDEPLOYED;
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
    	HttpResponse response = null;
    	DeploymentStatus deploymentStatus = null;
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
            deploymentStatus = new Gson().fromJson(responseString, DeploymentStatus.class);
    	}catch (Exception e) {
            logger.error(e.getMessage());
            throw new IOException(e);
        }

    	
    	return deploymentStatus.revision;
    }
    
    public static String activateBundleRevision1(ServerProfile profile, String revision, String type)
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

    public static String deleteBundle(ServerProfile profile)
            throws IOException {
    	if(profile.getApi_type()!=null && profile.getApi_type().equalsIgnoreCase("sharedflow")){
    		return deleteBundle(profile, "sharedflows");
    	}	
    	else{
    		return deleteBundle(profile, "apis");
    	}
    }
    
    public static String deleteBundle(ServerProfile profile, String type)
            throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept("application/json");
        headers.setContentType("application/octet-stream");
        HttpRequest deleteRestRequest = REQUEST_FACTORY.buildDeleteRequest(
                new GenericUrl(profile.getHostUrl() + "/"
                        + profile.getApi_version() + "/organizations/"
                        + profile.getOrg() + "/" + type + "/"
                        + profile.getApplication()));
        deleteRestRequest.setReadTimeout(0);
        deleteRestRequest.setHeaders(headers);
        executeAPI(profile, deleteRestRequest);
        return null;
    }

    public static String getVersionRevision() {
        return versionRevision;
    }

    public static void setVersionRevision(String versionRevision) {
        RestUtil.versionRevision = versionRevision;
    }
    
    /**
     * 
     * @param profile
     * @param request
     * @return
     * @throws IOException
     */
    private static HttpResponse executeAPI(ServerProfile profile, HttpRequest request) 
            throws IOException {
    	HttpHeaders headers = request.getHeaders();
    	try {
    		MgmtAPIClient client = new MgmtAPIClient();
    		if(profile.getBearerToken()!=null && !profile.getBearerToken().equalsIgnoreCase("")) {
    			logger.info("Using the bearer token");
    			accessToken = profile.getBearerToken();
    		}
    		else if(profile.getServiceAccountJSONFile()!=null && !profile.getServiceAccountJSONFile().equalsIgnoreCase("")) {
    			logger.info("Using the service account file to generate a token");
    			File serviceAccountJSON = new File(profile.getServiceAccountJSONFile());
    			accessToken = client.getGoogleAccessToken(serviceAccountJSON);
    		}
    		else {
    			logger.error("Service Account file or bearer token is missing");
				throw new IOException("Service Account file or bearer token is missing");
    		}
            logger.debug("**Access Token** "+ accessToken);
    		headers.setAuthorization("Bearer " + accessToken);
    	}catch (Exception e) {
            logger.error(e.getMessage());
            throw new IOException(e.getMessage());
         }
    	//fix for Issue153
    	headers.set("X-GOOG-API-FORMAT-VERSION", 2);
    	logger.info(PrintUtil.formatRequest(request));
        return request.execute();
    }

}
