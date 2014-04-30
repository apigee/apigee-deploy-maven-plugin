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
package io.apigee.buildTools.enterprise4g.utils;

import java.util.List;

import com.google.api.client.util.Key;

/**
 * maps to config items
 * @author sdey
 */

public class ConfigTokens { // represents configuration files containing all
							// tokens for an app bundle.

	@Key
	public List<Configurations> configurations;

	public class Configurations {

		@Key
		public String name;
		public List<Policy> policies; // for listing all policy level tokens in
										// step definition xmla
		public List<Policy> proxies; // for listing all proxy level tokens
										// inside proxies folder
		
		public List<Policy> targets; // for listing all target level tokens 
									 // inside targets folder
		
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public List<Policy> getPolicies() {
			return policies;
		}
		public void setPolicies(List<Policy> policies) {
			this.policies = policies;
		}
		public List<Policy> getProxies() {
			return proxies;
		}
		public void setProxies(List<Policy> proxies) {
			this.proxies = proxies;
		}
		
		public Policy getPolicyFileNameMatch(String name){
			for(int i=0;i<this.policies.size();i++)
				if (this.policies.get(i).name.equals(name)){
					return this.policies.get(i);
				}
			
			return null; 
		}
		
		public Policy getProxyFileNameMatch(String name){
			for(int i=0;i<this.proxies.size();i++)
				if (this.proxies.get(i).name.equals(name)){
					return this.proxies.get(i);
				}
			
			return null; 
		}
		
		public Policy getTargetFileNameMatch(String name){
			for(int i=0;i<this.targets.size();i++)
				if (this.targets.get(i).name.equals(name)){
					return this.targets.get(i);
				}
			
			return null; 
		}
		public List<Policy> getTargets() {
			return targets;
		}
		public void setTargets(List<Policy> targets) {
			this.targets = targets;
		}

	}

	// Token is a key value pair for each environment specific policy element,
	// per environment

	public class Token {

		@Key
		public String xpath;

		@Key
		public String value;

		public String getXpath() {
			return xpath;
		}

		public void setXpath(String xpath) {
			this.xpath = xpath;
		}

		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}
	}

	// policy here is for listing all tokens for a XML policy file or a API
	// Proxy xml file

	public class Policy {

		@Key
		public String name;
		@Key
		public List<Token> tokens;
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public List<Token> getTokens() {
			return tokens;
		}
		public void setTokens(List<Token> tokens) {
			this.tokens = tokens;
		}
	}

	public List<Configurations> getConfigurations() {
		return configurations;
	}

	public void setConfigurations(List<Configurations> configurations) {
		this.configurations = configurations;
	}
	
	public Configurations getConfigbyEnv(String name){
		for(int i=0;i<this.configurations.size();i++)
			if (this.configurations.get(i).name.equals(name)){
				return this.configurations.get(i);
			}
		
		return null;
	}

}
