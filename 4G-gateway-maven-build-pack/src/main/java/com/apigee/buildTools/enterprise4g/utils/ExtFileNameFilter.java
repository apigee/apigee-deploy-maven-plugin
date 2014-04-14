package com.apigee.buildTools.enterprise4g.utils;

import java.io.File;

public class ExtFileNameFilter implements java.io.FilenameFilter {
	String ext;

	public ExtFileNameFilter(String ext) {
		this.ext = "." + ext;
	}

	public boolean accept(File dir, String name) {
		return name.toLowerCase().endsWith(".xml");
	}
}
