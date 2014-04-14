package com.apigee.buildTools.enterprise4g.rest;

import java.io.IOException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;

import org.apache.axis2.transport.nhttp.HostnameVerifier;

public class FakeHostnameVerifier implements HostnameVerifier {

	public void check(String arg0, SSLSocket arg1) throws IOException {
		// TODO Auto-generated method stub
		
	}

	public void check(String arg0, X509Certificate arg1) throws SSLException {
		// TODO Auto-generated method stub
		
	}

	public void check(String[] arg0, SSLSocket arg1) throws IOException {
		// TODO Auto-generated method stub
		
	}

	public void check(String[] arg0, X509Certificate arg1) throws SSLException {
		// TODO Auto-generated method stub
		
	}

	public void check(String arg0, String[] arg1, String[] arg2)
			throws SSLException {
		// TODO Auto-generated method stub
		
	}

	public void check(String[] arg0, String[] arg1, String[] arg2)
			throws SSLException {
		// TODO Auto-generated method stub
		
	}

	public boolean verify(String arg0, SSLSession arg1) {
		// TODO Auto-generated method stub
		return true;
	}

}
