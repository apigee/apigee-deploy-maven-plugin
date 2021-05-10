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
package io.apigee.buildTools.enterprise4g.rest;

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
