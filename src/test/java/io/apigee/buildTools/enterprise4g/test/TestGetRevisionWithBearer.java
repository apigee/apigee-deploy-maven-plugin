/**
 * Copyright (C) 2016 Apigee Corporation
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
package io.apigee.buildTools.enterprise4g.test;

import io.apigee.buildTools.enterprise4g.rest.RestUtil;
import io.apigee.buildTools.enterprise4g.utils.ServerProfile;
import com.apigee.mgmtapi.sdk.client.MgmtAPIClient;
import com.apigee.mgmtapi.sdk.model.AccessToken;
import junit.framework.TestCase;

import java.io.IOException;

public class TestGetRevisionWithBearer extends TestCase {
	
	ServerProfile profile = new ServerProfile();
	
	protected void setUp() throws Exception {
		super.setUp();

        profile.setHostUrl("https://api.enterprise.apigee.com");
        profile.setApplication("taskservice");
        profile.setCredential_user(System.getProperty("username"));
        profile.setCredential_pwd(System.getProperty("password"));
        profile.setEnvironment(System.getProperty("env"));
        profile.setOrg(System.getProperty("org"));
        profile.setApi_version("v1");
        profile.setBearerToken(generateAccessToken());
        profile.setTokenUrl("https://login.apigee.com/oauth/token");

    }
	
	public void testGetRevisionCall() throws IOException{
		RestUtil.getRevision(profile);
		System.out.println("revision number::"+ RestUtil.getVersionRevision());
		assertNotNull(RestUtil.getVersionRevision());
	}
	
	public void testGetLatestRevisionCall() throws IOException{
		String latestRev = RestUtil.getLatestRevision(profile);
		assertNotNull(latestRev);
	}

    //the client can generate the token using any other plugin they choose
    private String generateAccessToken() throws Exception{
        MgmtAPIClient client = new MgmtAPIClient();
        AccessToken token = client.getAccessToken(
                            "https://login.apigee.com/oauth/token",
                            "edgecli", "edgeclisecret",
                            System.getProperty("username"),
                            System.getProperty("password"));
        return token.getAccess_token();
    }

}
