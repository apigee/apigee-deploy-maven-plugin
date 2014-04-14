package com.apigee.buildTools.enterprise4g.test;

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
		//$JUnit-END$
		return suite;
	}

}
