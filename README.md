----------------
About the Plugin
----------------

apigee-edge-maven-plugin is a build and deploy utility for building and deploying the Apigee ApiProxy's/Application bundles into Apigee Edge Platform. 
The code is distributed under the Apache License 2.0.


-------------------------------------------
Detailed documentation on the use of plugin
-------------------------------------------

Refer [Maven Deployment Guide](https://github.com/apigee/apigee-deploy-maven-plugin/blob/master/BuildingAndDeployingAPIBundles.docx)


----------------------------------------------------------------
For the users migrating from  Apigee maven repo to maven central
----------------------------------------------------------------

The plugin was hosted in Apigee maven repo and is now moved to maven central for public consumption. We advice all the existing user to move to the new repo for latest updates and enhancements.
(**Repo  Apigee url** :- http://repo.apigee.com:8081/artifactory/repo)

This open source version is taken from the Version **0.0.16** of **4G-gateway-maven-build-pack**.
All the features available till 0.0.16 is moved on to the open source version and the older one in closed out for any development internally or externally.

The artifact id , group  Id and version needs to be updated across all the poms

groupId : com.apigee.build-tools.enterprise4g  to io.apigee.build-tools.enterprise4g
artifactId :  4G-gateway-maven-build-pack  to apigee-edge-maven-plugin
version  change: 0.0.X to 1.0.0  (The latest one in maven central)

In Parent pom
-------------

  1.    <pluginManagement>
			<plugins>
				<plugin>
					<groupId>io.apigee.build-tools.enterprise4g</groupId>
					<artifactId>apigee-edge-maven-plugin</artifactId>
					<version>1.0.0</version>
				</plugin>
			</plugins>
		</pluginManagement>

 2.

		<plugin>
        				<groupId>io.apigee.build-tools.enterprise4g</groupId>
        				<artifactId>apigee-edge-maven-plugin</artifactId>
        				<configuration>
        				<skip>true</skip>
        				</configuration>
        </plugin>


In Child pom
------------

1. Update the  plugin

			<plugin>
				<groupId>io.apigee.build-tools.enterprise4g</groupId>
				<artifactId>apigee-edge-maven-plugin</artifactId>
				<configuration>
					<skip>false</skip> <!-- Use this module level config to skip module build. Make it true -->
				</configuration>
				<executions>
			    <execution>
		        <id>configure-bundle-step</id>
		        <phase>package</phase>
		        <goals>
		       		<goal>configure</goal>
		        </goals>
			    </execution>
		     </executions>
			</plugin>





------------------------------------------
Recommended Convention for Contributions
------------------------------------------

1. Open an Issue in Git
2. Send a pull request for integration with main code base
3. Make sure to include valid test cases for teh feature and the existing test case should continue passing
4. Moderator of the code will merge after review

------------------------------------
Commands for newbee Plugin developers
-------------------------------------

*Install/compile the Plugin*    

mvn clean install -Dmaven.test.skip=true

*Deploy the plugin to repository* 

mvn deploy -Dmaven.test.skip=true

*Maven Describe Output* 

Use maven describe to get more details on the available goals and on each of individual goals.

*For available Goals*

mvn help:describe -Dplugin=apigee-enterprise

*For 'configure' Goal* 

mvn help:describe -Dplugin=apigee-enterprise -Dmojo=configure -Dfull=true

*For 'deploy' Goal* 

mvn help:describe -Dplugin=apigee-enterprise -Dmojo=deploy -Dfull=true

*Command to execute for testing*

mvn clean install -Dusername=<username> -Dpassword=<password> -Dorg=<org> -Denv=<env>

Note:- The Plugin development expects the developer to have an Apigee Org and access to test the Plugins.
The information is configured in default profile of the pom.


