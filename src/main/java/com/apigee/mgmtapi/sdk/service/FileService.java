package com.apigee.mgmtapi.sdk.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

@Service("fileService")
public class FileService {
	
	@Value("${mgmt.host}")
    private String mgmtHost;
	
	@Value("${mgmt.login.url}")
    private String mgmtloginUrl;
 
    @Value("${mgmt.login.client.id}")
    private String clientId;
    
    @Value("${mgmt.login.client.secret}")
    private String clientSecret;
 
    @Autowired
    private Environment environment;

	public String getMgmtHost() {
		return mgmtHost;
	}

	public String getMgmtloginUrl() {
		return mgmtloginUrl;
	}

	public String getClientId() {
		return clientId;
	}

	public String getClientSecret() {
		return clientSecret;
	}

	public Environment getEnvironment() {
		return environment;
	}

	/*public void readValues() {
        System.out.println("Getting property via Spring Environment :"
                + environment.getProperty("mgmt.client.id"));
        System.out.println("mgmtloginUrl : " + mgmtloginUrl);
        System.out.println("mgmtHost : " + mgmtHost);
         
    }*/
    
    
}
