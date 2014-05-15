This is the open source code base for apigee-edge-maven-plugin .
apigee-edge-maven-plugin is a build and deploy utility for building and deploying the Application bundles into Apigee Edge.


-----------------------------------------------------------------------------------------------------------------------------------
Note for Existing users who uses the plugin out of Apigee maven repo http://repo.apigee.com:8081/artifactory/repo
-----------------------------------------------------------------------------------------------------------------------------------

This open source version is taken from the Version 0.0.16 of  4G-gateway-maven-build-pack
All the features available till 0.0.16 is moved on to the open source version and the older one in closed out for any development internally or externally.


------------------
Note for New users
------------------

The code is distributed under the Apache License 2.0.

------------------------------------------
Recommended Convention for any Enhancement
------------------------------------------

1. Open an Issue in Git
2. Send a pull request for integration with main code base
3. 

--------
Commands
--------

To Install/compile the Plugin  : mvn clean install -Dmaven.test.skip=true

To deploy the plugin to repository : mvn deploy -Dmaven.test.skip=true

Maven Describe Output : Use maven describe to get more details on the available goals and on each of individual goals.

For available Goals : mvn help:describe -Dplugin=apigee-enterprise

For 'configure' Goal : mvn help:describe -Dplugin=apigee-enterprise -Dmojo=configure -Dfull=true

For 'deploy' Goal : mvn help:describe -Dplugin=apigee-enterprise -Dmojo=deploy -Dfull=true

-----
Note
------

The Plugin development expects the developer to have an Apigee Org and access to test the Plugins.
The information is configured in default profile of the pom.

------------------------------
Command to execute for testing
------------------------------

mvn clean install -Dusername=<username> -Dpassword=<password> -Dorg=<org> -Denv=<env>



