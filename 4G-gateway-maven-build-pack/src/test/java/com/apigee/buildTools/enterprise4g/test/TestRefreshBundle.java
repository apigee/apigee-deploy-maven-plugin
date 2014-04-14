package com.apigee.buildTools.enterprise4g.test;

import com.apigee.buildTools.enterprise4g.utils.ServerProfile;
import junit.framework.TestCase;

import java.io.IOException;

public class TestRefreshBundle extends TestCase {
	
	ServerProfile profile = new ServerProfile();
	
	protected void setUp() throws Exception {
		super.setUp();
		profile.setHostUrl("https://api.enterprise.apigee.com");
		profile.setApplication("taskservice");
        // replace with your own credentials
        profile.setCredential_user("sdey+testmaven@apigee.com");
        profile.setCredential_pwd("5KdwtZUw");
		profile.setEnvironment("prod");
		profile.setOrg("apigee-cs");
		profile.setApi_version("v1");
		profile.setProfileId("ts_prod");
		
	}
	
	
	
	public void testRefreshBundleCall() throws IOException{
		String revision;
		com.apigee.buildTools.enterprise4g.rest.RestUtil.getRevision(profile);
		revision = com.apigee.buildTools.enterprise4g.rest.RestUtil.getVersionRevision();
		//String newRevision = Integer.toString((Integer.parseInt(revision)+1));
		
		String status = com.apigee.buildTools.enterprise4g.rest.RestUtil.refreshBundle(profile, revision); //Integer.toString(newRevision)
		System.out.println("deployment status::"+status);
		assertNotNull(status); 
	}

}
