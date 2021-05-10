------------------------------------------
Recommended Convention for Contributions
------------------------------------------

1. Open an Issue in Git
2. Send a pull request for integration with main code base
3. Make sure to include valid test cases for teh feature and the existing test case should continue passing
4. Moderator of the code will merge after review


------------------------------------
Commands to get started for newbee Plugin(Tool) developers. (This is not for Plugin Users)
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
