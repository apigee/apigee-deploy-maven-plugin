/**
 * Copyright (C) 2016 Apigee Corporation
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

import static org.junit.Assert.*;

import org.junit.Test;

import io.apigee.buildTools.enterprise4g.utils.ServerProfile;
import com.apigee.mgmtapi.sdk.client.MgmtAPIClient;
import com.apigee.mgmtapi.sdk.model.AccessToken;

import java.io.IOException;

public class TestGetRevisionWithRefresh extends RestClientTestBase {

	@Override
	protected Bundle createBundle() {
		return new Bundle("taskservice");
	}

	@Override
	protected void configureProfile(ServerProfile profile) {
		try {
			AccessToken t1 = generateAccessToken();
			AccessToken t2 = generateAccessTokenFromRefreshToken(t1.getRefresh_token());
			profile.setBearerToken(t2.getAccess_token());
		} catch (Exception e) {
			throw new RuntimeException("Failed to get access tokens", e);
		}
	}

	@Test
	public void testGetRevisionCall() throws IOException {
		Long latestRevision = createClient().getLatestRevision(getBundle());
		log.info("revision number::{}", latestRevision);
		assertNotNull(latestRevision);
	}

	//the client can generate the token using any other plugin they choose
	private AccessToken generateAccessToken() throws Exception {
		ServerProfile p = getProfile();
		MgmtAPIClient client = new MgmtAPIClient();
		AccessToken token = client.getAccessToken(
				p.getTokenUrl(),
				p.getClientId(),
				p.getClientSecret(),
				p.getCredential_user(),
				p.getCredential_pwd());
		return token;
	}

	//the client can generate the token using any other plugin they choose
	private AccessToken generateAccessTokenFromRefreshToken(String refreshToken) throws Exception {
		ServerProfile p = getProfile();
		MgmtAPIClient client = new MgmtAPIClient();
		AccessToken token = client.getAccessTokenFromRefreshToken(p.getTokenUrl(),
				p.getClientId(),
				p.getClientSecret(), refreshToken);
		return token;
	}

}
