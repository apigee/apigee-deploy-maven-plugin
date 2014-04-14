package com.apigee.buildTools.enterprise4g.test;

import com.apigee.buildTools.enterprise4g.utils.ServerProfile;
import junit.framework.TestCase;

import java.io.IOException;

public class TestDeleteDeployedBundle extends TestCase{

	
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
	
	public void testDeleteDeployedBundleCall() throws IOException{
		try {
            String revision = com.apigee.buildTools.enterprise4g.rest.RestUtil.getDeployedRevision(profile);
            String deleted = com.apigee.buildTools.enterprise4g.rest.RestUtil.deleteBundle(profile, revision);
			assertNotNull(deleted);
			System.out.println("deleted revision:: "+ deleted);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
 
	}
	
}
