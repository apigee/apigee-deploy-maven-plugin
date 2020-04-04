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

import static org.junit.Assert.*;

import org.junit.Test;

public class TestDeleteDeployedBundle extends RestClientTestBase {

	@Override
	protected Bundle createBundle() {
		return new Bundle("taskservice");
	}

	@Test
	public void testDeleteDeployedBundleCall() throws Exception {
		RestClient client = createClient();
		Long deployedRevision = client.getDeployedRevision(getBundle());
		Long deletedRevision = client.deleteBundle(getBundle().clone(deployedRevision));
		assertNotNull(deletedRevision);
		log.info("deleted revision::{}", deletedRevision);
	}

}
