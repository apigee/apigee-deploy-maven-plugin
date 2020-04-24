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

import io.apigee.buildTools.enterprise4g.utils.ServerProfile;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class RestClientTestBase {

	protected Logger log = LoggerFactory.getLogger(getClass());

	private ServerProfile profile;

	private Bundle bundle;

	@Before
	public void setUp() throws Exception {
		// configure a server profile from defaults and system settings
		profile = new ServerProfile();
		profile.setHostUrl("https://api.enterprise.apigee.com");
		profile.setCredential_user(System.getProperty("username"));
		profile.setCredential_pwd(System.getProperty("password"));
		profile.setEnvironment(System.getProperty("env"));
		profile.setOrg(System.getProperty("org"));
		profile.setApi_version("v1");
		profile.setTokenUrl("https://login.apigee.com/oauth/token");
		configureProfile(profile);
		// configure the test bundle
		bundle = createBundle();
	}

	/**
	 * Can be overridden in tests to configure the profile further.
	 *
	 * @param profile the profile that can be further configured
	 */
	protected void configureProfile(ServerProfile profile) {
		// noop
	}

	/**
	 * Return the configured server profile for the test
	 *
	 * @return a valid server profile
	 */
	protected ServerProfile getProfile() {
		return profile;
	}

	/**
	 * Create the bundle configuration that can be used for testing.
	 *
	 * @return a valid bundle configuration
	 */
	protected abstract Bundle createBundle();

	/**
	 * Return the configured bundle
	 *
	 * @return valid bundle configuration
	 */
	protected Bundle getBundle() {
		return bundle;
	}

	/**
	 * Creates a rest client for the configured profile.
	 *
	 * @return a configured rest client
	 */
	protected RestClient createClient() {
		return new RestClient(getProfile());
	}

}
