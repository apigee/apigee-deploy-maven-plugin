package com.apigee.buildTools.enterprise4g.test;

import com.apigee.buildTools.enterprise4g.utils.ServerProfile;
import junit.framework.TestCase;

import java.io.IOException;

public class TestUploadBundle extends TestCase {
	
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
	
	public void testUploadBundleCall() throws IOException{
		com.apigee.buildTools.enterprise4g.rest.RestUtil.uploadBundle(profile, "4G-gateway-maven-build-pack/src/test/java/com/apigee/buildTools/enterprise4g/test/testdata/taskservice1.zip");
		System.out.println("revision number::"+com.apigee.buildTools.enterprise4g.rest.RestUtil.getVersionRevision());
		assertNotNull(com.apigee.buildTools.enterprise4g.rest.RestUtil.getVersionRevision()); 
	}
		
	}


