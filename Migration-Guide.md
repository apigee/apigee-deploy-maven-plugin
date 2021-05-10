
----------------------------------------------------------------
For the users migrating from  Apigee maven repo to maven central
----------------------------------------------------------------

The plugin was hosted in Apigee maven repo and is now moved to maven central for public consumption. We advice all the existing user to move to the new repo for latest updates and enhancements.
(**Repo  Apigee url** :- http://repo.apigee.com:8081/artifactory/repo)

This open source version is taken from the Version **0.0.16** of **4G-gateway-maven-build-pack**.
All the features available till 0.0.16 is moved on to the open source version and the older one in closed out for any development internally or externally.

**Steps for Migrating from old repo to new one**

1. The artifact id , group  Id and version needs to be updated across all the poms

   **groupId** : Update com.apigee.build-tools.enterprise4g  to io                   .apigee.build-tools.enterprise4g

   **artifactId** : Update 4G-gateway-maven-build-pack  to apigee                       -edge-maven-plugin

   **version  change** :Update 0.0.X to 1.0.0  (The latest one in                           maven central)

2.  Update *Parent pom* under *pluginManagement* and *plugin* sections

**a**

        <pluginManagement>
			<plugins>
				<plugin>
					<groupId>io.apigee.build-tools.enterprise4g                          </groupId>
					   <artifactId>apigee-edge-maven-plugin
					   </artifactId>
					<version>1.0.0</version>
				</plugin>
			</plugins>
		</pluginManagement>

 **b**

		<plugin>
          <groupId>io.apigee.build-tools.enterprise4g</groupId>
        	<artifactId>apigee-edge-maven-plugin</artifactId>
        	  <configuration>
        	   <skip>true</skip>
        	  </configuration>
         </plugin>




1. Update the *Child pom* update the below 

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




