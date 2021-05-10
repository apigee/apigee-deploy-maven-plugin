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
package io.apigee.buildTools.enterprise4g.test;

import junit.framework.Test;
import junit.framework.TestSuite;

public class deploymentTestSuite {

	public static Test suite() {
		TestSuite suite = new TestSuite(deploymentTestSuite.class.getName());
		//$JUnit-BEGIN$
        suite.addTestSuite(TestUploadBundle.class);
        suite.addTestSuite(TestGetRevision.class);
		suite.addTestSuite(TestGetDeployedRevision.class);
		suite.addTestSuite(TestRefreshBundle.class);
        suite.addTestSuite(TestDeleteDeployedBundle.class);
		suite.addTestSuite(TestGetRevisionWithBearer.class);
		suite.addTestSuite(TestGetRevisionWithMFA.class);
		suite.addTestSuite(TestGetRevisionWithRefresh.class);
		//$JUnit-END$
		return suite;
	}

}
