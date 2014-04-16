package com.apigee.cs.ast;

import java.io.File;
import java.io.IOException;

import junit.framework.Assert;

import org.jruby.embed.EvalFailedException;
import org.junit.Test;

import com.apigee.cs.util.ZipUtils;

public class BundleConfigurerTest {

	File testSourceBundle;
	public File getTestSourceBundle() throws IOException {
		if (testSourceBundle!=null) {
			return testSourceBundle;
		}
		
		File tempDir = new File("./target");
		tempDir.mkdirs();
		
		File f = new File(tempDir,"test-bundle.zip");
		
		ZipUtils zu = new ZipUtils();
		zu.zipDir(f, new File("./src/test/resources/test-bundle"),"application");
		testSourceBundle=f;
		return testSourceBundle;
		
	}
	@Test
	public void testConfigureBundle() throws IOException {
		BundleConfigurer bc = new BundleConfigurer();
		bc.createTempWorkspace();
		bc.unzipBundleIntoWorkspace(getTestSourceBundle());
		bc.setTargetBundle(new File("test.zip"));
		bc.setProfileName("test-profile");
		bc.configure();
	}
	
	
	@Test(expected=RuntimeException.class)
	public void testInvalidProfile() throws IOException {
		BundleConfigurer bc = new BundleConfigurer();
		bc.createTempWorkspace();
		bc.unzipBundleIntoWorkspace(getTestSourceBundle());
		bc.setTargetBundle(new File("test.zip"));
		bc.setProfileName("invalid-profile");
		bc.configure();
	}
	@Test(expected=IllegalStateException.class)
	public void testWithoutSettingWorkspace() throws IOException {
		BundleConfigurer bc = new BundleConfigurer();
		bc.unzipBundleIntoWorkspace(getTestSourceBundle());
		
	}
	
	@Test
	public void testEnvironmentName() {
		BundleConfigurer bc = new BundleConfigurer();
		Assert.assertNull(bc.getProfileName());
		bc.setProfileName("test");
		Assert.assertEquals("test",bc.getProfileName());
	}
	
}
