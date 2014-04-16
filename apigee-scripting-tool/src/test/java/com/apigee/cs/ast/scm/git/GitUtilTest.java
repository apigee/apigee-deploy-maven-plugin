package com.apigee.cs.ast.scm.git;

import java.io.File;

import org.junit.Test;

public class GitUtilTest {

	
	@Test (expected=IllegalStateException.class)
	public void testJGit() {
		GitUtil gu = new GitUtil(new File("/some/invalid/path"));
		
		
	}
}
