----------------
About the Plugin
----------------

apigee-edge-maven-plugin is a build and deploy utility for building and deploying the Apigee ApiProxy's/Application bundles into Apigee Edge Platform. 
The code is distributed under the Apache License 2.0.


-------------------------------------------
##Detailed documentation on the use of plugin
-------------------------------------------



# Contents

- [Getting-Started](#getting-started)

- [Create a new API](#create-a-new-api)

- [Building API bundles](#building-api-bundles)

- [Steps to set it up](#steps-to-set-it-up) 

- [Step 1 :Create a maven compatible file structure](#step1-create-a-maven-compatible-file-structure)
- [Step 2 : Create and configure pom files](#step-2-create-and-configure-pom-files)
- [Step 3: Create and configure config.json](#step-3-create-and-configure-config-json)

- [parent-pom/pom.xml Sample](#parent-pom-pom-xml-sample) 

- [pom.xml Sample](#pom-xml-sample)

- [Config.json Sample](#config-json-sample)


# Getting Started


Often the most difficult and confusing aspect of application development is figuring out how to build a common framework for creating new applications. Over time development teams have started using tools like Maven, Ant and Ivy to automate some of these functions. This plugin uses the maven plugin for deploying Apigee bundles to the Edge platform. 

####Why this deployment plugin is developed ?

- Helps in offline development 
- Easy Integrations with Source control like git
- The maven build strategy is a good strategy if your current enterprise build and deploy strategies already use Maven or Maven compatible tools. 
- Configuration management across organizations/Environments
(Realtime Enterprise Edge architechture consists of multiple organizations/environments and configurations which varies according to these. An Example Scenario for this use would be the maven configuration file to replace the number of requests it takes to trip a spike arrest policy. In your non-production environment you may want this policy to take effect when 10 requests a minute is surpassed, in production you may want this policy to trip when 10000 requests a minute is surpassed.)



## Building API bundles

### What is an Apigee bundle? 

Apigee bundles can be described as a zipped file system composed of configuration, scripts and code. The file system when extracted is composed of the following structure.

|-apiproxy

 |---proxies

 |---resources

 |---policies

 |---targets


### Create a new API

Instructions for creating a new API can be found at this link

[http://apigee.com/docs/api-platform/content/add-and-configure-your-first-api](http://apigee.com/docs/api-platform/content/add-and-configure-your-first-api)

### Export your API

Instructions for exporting your API via an API can be found at this link

[http://apigee.com/docs/api/api\_methods/105-export-an-api](http://apigee.com/docs/api/api_methods/105-export-an-api)

Instructions for exporting you API using the UI can be found at this link

[http://apigee.com/docs/api-platform/content/edit-api-revisions-offline#-a-class-jumplink-name-export-a-downloading-an-api-revision](http://apigee.com/docs/api-platform/content/edit-api-revisions-offline#-a-class-jumplink-name-export-a-downloading-an-api-revision)


##Steps to set it up 

Follow below steps to set up your local development Environment

1. Create the folder structure as described in the  section
2. Create and configure pom files -(Parent pom and the child pom for teh proxy)
3. Create and configure config.json - if there are environment specific configurat    ions (This is an optional step)
  
And you are ready for deploy to APigee Edge using the plugin


###Step1 Create a maven compatible file structure


Below is the recommended structure for the Project . However only the folder structure below the folder gateway is mandatory 

|-name-of-root (typically company name)

 |---archive

 |-----docs

 |-----src

 |---binaries

 |---build-artifacts

 |---docs

 |-----customer

 |-----reference

 |-----solution

 |---src

 |-----analytics

 |-----gateway

** |-------parent-pom**

 |-------test-app

 |---------apiproxy

 |-----------proxies

 |-----------resources

 |-------------py

 |-----------policies

 |-----------targets

 |-----java

 |-----portal

 |---test


#### Decompress API Bundle

The API bundle will come zipped, to achieve access to the contents use the un archiving utility of choice and unzip the file.

Once unzipped you will have a folder named apiproxy, this folder contains all of the configuration for your proxy and the folder structure needed for re-importing of the configuration.

The composition of the folder can be described as below.

{ApiName}.xml - A file that contains descriptors for the content

policies - A folder that contains xml policies

proxies - A folder that contains information about your proxy configurations (inbound)

targets - A folder that contains information about target configurations (outbound)

resources - A folder that contains any scripts (Java, js, and python)

Note: when creating scripts place your scripts in folders with the name of the script ext i.e. java, jsc, py



### Step 2 Create and configure pom files


In a standard configuration typically we have parent-pom (pom.xml inside the *parent-pom* directory) and a child pom(pom file at  the peer level as folder *apiproxy*).

Parent-pom :-The contents of the parent pom folder will contain a single pom.xml file. This file typically contains most of the configuration of maven and the plugin, it also contains credentials for the Apigee platform. In case of manual creating a Maven compatible file structure,  "parent-pom"  directory should be in perr level  with Customer application folders 
Here we configure all the information whch is same acroos multiple apiproxys. Eg :- Profile configurations which has the org/env info etc 

#### parent-pom-pom-xml Sample

Refer parent-pom template [parent-pom]
(https://github.com/apigee/apigee-deploy-maven-plugin/blob/master/samples/forecastweatherapi-parentpom/src/gateway/parent-pom/pom.xml)

Child-pom :- Here we configure all the details specific to the particular proxy.

#### pom-xml Sample

Refer child-pom template [child-pom]
(https://github.com/apigee/apigee-deploy-maven-plugin/blob/master/samples/forecastweatherapi-parentpom/src/gateway/forecast/pom.xml).



**groupId** element's content should match that of the same element in the parent pom.xml. 

**artifactId** element's content should be a unique name, typically set to the folder name or the name of the API.

**name** element's content should match the artifact name.

### Step 3 Create and configure config-json


The config.json acts as your build time configuration modification descriptor. The file's root object is called configurations, configurations is an array of proxy configurations scoped to an environment. Note: it is important that the name of the configurations match the name of the profiles in the parent-pom.

for instance in the example below you have two configurations one for the test profile and one for the production profile. This example also shows how you can use xpath to replace environment specific settings.

#### Config-json Sample


Refer config.json template [config.json]
(https://github.com/apigee/apigee-deploy-maven-plugin/blob/master/samples/forecastweatherapi-parentpom/src/gateway/forecast/config.json)



     **configuration** - an array of API definitions

        **name** - Name of the maven profile

         **proxies** - Array of proxy definitions, directly                    correlates to the proxies folder in your API Bundle

        **name** - Name of file to configure

        **tokens** - Array of Actions to Invoke on Elements

            **xpath** - Path to element that your want to change the                         value of

            **value** - The replacement value

           **policies** - Array of proxy definitions, directly                                correlates to the policies or step                                  definition folder in your API Bundle

           **name** - Name of file to configure

           **tokens** - Array of Actions to Invoke on Elements

           **xpath** - Path to element that your want to change the                        value of

           **value** - The replacement value

           **targets** - Array of proxy definitions, directly                       correlates to the proxies folder in your API Bundle

           **name** - Name of file to configure

           **tokens** - Array of Actions to Invoke on Elements

           **xpath** - Path to element that your want to change the             value of

           **value** - The replacement value



Deploy and run the scripts

Command

*To deploy the proxy*

/src/gateway/proxy-dir
(run the command from the directory same as child pom)

**mvn apigee-enterprise:deploy -P -Dusername=<username> -Dpassword=<password>**

For example:

mvn apigee-enterprise:deploy -Pprod -Dusername=admin@toopowerful.com -Dpassword=too\_powerful\_password

*To deploy the proxy and run jmeter tests*

**mvn install -P  <profile_name> -Dusername=<username> -Dpassword=<password>**




## Advanced Configuration Options



**Note1** . The following entries in some XML file elements could be changed to match Customer environment: "groupId", "id" (for each profile sections), "apigee.profile", "apigee.env", "apigee.hosturl", "apigee.org". The contents of "apigee.profile", "apigee.env", and "id" elements should match the profile the Customer wants to use and is matched with environment name. The value of the "apigee.hosturl" element shouldmatch the value in the example if Customer is an enterprise cloud user. If Customer is an on-premise user this url would be the location of Customer management servers host and port. Port is by default 8080. Thevalue of the "apigee.org" element should match the organization provided when Customer environment was initially setup, in most cases this includes the name of the company. For on premise installations, the org is setup when you run installation scripts. The maven group id is malleable and is also marked in red for both pom examples, the only thing you should note when changing this is that they need to be consistent between applications.



**Note2** . The"apigee.override.delay", "apigee.delay,apigee.options" are optional elements. The"apigee.override.delay" could bespecified (in milliseconds) . This will ensure to add a delay between the operations like delete, import, activate, deactivate etc .

**Note3** . The "apigee.options" element can have the following values: **clean** (this option will delete the last deployed revision in an environment), **validate** (this option will validate a bundle before importing . Thus if you want strict validation then its required), **inactive** (this option will import the bundle without activating the bundle), **override** (this option is used for seamless deployment and should be supplied withapigee.override.delayparameter. The apigee.override.delay expects delay to be given in seconds), **update** (this optionwill update the deployed revision . This is similar to import with validation but no new revision is created. If there any errors in the bundle, error is thrown and the existing bundle is left intact.In case the revision they are trying to update is deployed, it will internally trigger undeployment and deployment. It is completely in the background and not visible in the response. **It is advisednot to updatethe deployed revision** . (UI could show a warning or something in this case).

**Note4** . The"apigee.options" combination could be given with comma-separatedvalues. The precedence order of options are -> override , update , (clean, inactive, validate, force). 

**Note5** . Flow without "apigee.options":import–>undeploy(lastactive)–>deploy (new revision)





----------------------------------------------------------------
For the users migrating from  Apigee maven repo to maven central
----------------------------------------------------------------

The plugin was hosted in Apigee maven repo and is now moved to maven central for public consumption. We advice all the existing user to move to the new repo for latest updates and enhancements.
(**Repo  Apigee url** :- http://repo.apigee.com:8081/artifactory/repo)

This open source version is taken from the Version **0.0.16** of **4G-gateway-maven-build-pack**.
All the features available till 0.0.16 is moved on to the open source version and the older one in closed out for any development internally or externally.

Refer for detailed documentation [Guide for Users Migrating from Apigee repo]
(https://github.com/apigee/apigee-deploy-maven-plugin/blob/master/Migration-Guide.md)

------------------------------------------
Recommended Convention for Contributions
------------------------------------------

Refer [Guide for Plugin Developers] (https://github.com/apigee/apigee-deploy-maven-plugin/blob/master/PluginDevelopers-Guide.md)


People Involved
------------------------

The plugin is initially developed  by [Santany Dey] (sdey@apigee.com). With major contributions from [Rajesh Mishra] (rajesh.mishra@apigee.com) and others listed in the pom developer list.
It was open sourced by [Priyanky Thomas] (priyanky@apigee.com).


