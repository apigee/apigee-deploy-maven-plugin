----------------
About the Plugin
----------------

apigee-edge-maven-plugin is a build and deploy utility for building and deploying the Apigee ApiProxy's/Application bundles into Apigee Edge Platform.
The code is distributed under the Apache License 2.0.

------------
TL;DR
------------

The [samples folder](https://github.com/apigee/apigee-deploy-maven-plugin/tree/master/samples) provides a Readme with Getting Started steps and commands to hit the ground quickly.

------------
Video
------------

Learn more, check out this video! [Ask the Expert](http://academy.apigee.com/ask-expert#maven)

-------------------------------------------
##Detailed documentation on the use of plugin
-------------------------------------------

## Contents

- [Getting-Started](#getting-started)
- [Building API bundles](#building-api-bundles)
- [Steps to set it up](#steps-to-set-it-up)
- [Step 1: Create a maven compatible file structure](#step-1-create-a-maven-compatible-file-structure)
- [Step 2: Create and configure pom files](#step-2-create-and-configure-pom-files)
- [Step 3: Create and configure config.json](#step-3-create-and-configure-config-json)
- [parent-pom/pom.xml Sample](#parent-pom-pom-xml-sample)
- [pom.xml Sample](#pom-xml-sample)
- [Config.json Sample](#config-json-sample)
- [Commands](#commands-for-deploying-the-proxy-using-maven)
- [Node.js Application Support](#deploying-api-proxies-with-nodejs-apps)

# Getting Started

Often the most difficult and confusing aspect of application development is figuring out how to build a common framework for creating new applications. Over time, development teams have started using tools like Maven, Ant and Ivy to automate some of these functions. This plugin uses the Maven plugin for deploying Apigee bundles to the Edge platform.

#### Why this deployment plugin is developed?

- Helps in offline development.
- Easy integrations with source control like git.
- The Maven build strategy is a good strategy if your current enterprise build and deploy strategies already use Maven or Maven compatible tools.
- Configuration management across organizations/environments.
(Realtime Enterprise Edge architecture consists of multiple organizations/environments and configurations which varies according to these. An example scenario for this use would be the Maven configuration file to replace the number of requests it takes to trip a spike arrest policy. In your non-production environment you may want this policy to take effect when 10 requests a minute is surpassed, in production you may want this policy to trip when 10000 requests a minute is surpassed.)



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

To export you API via Apigee Edge select the organization that contains the proxy you wish to export. From the navigation bar on the top, under APIs select the API Proxies. Select the proxy, on the far left of the screen, under the drop down menu titled Project, select Download Current Revision.

## Steps to set it up

Follow below steps to set up your local development environment

1. Create the folder structure as described in the  section
2. Create and configure pom files - Parent pom and the child pom for the proxy
3. Create and configure config.json - if there are environment specific configurations (This is an optional step)

And you are ready for deploy to Apigee Edge using the plugin


###Step 1 Create a Maven compatible file structure


Below is the recommended structure for the project. However only the folder structure below the folder gateway is mandatory

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

The API bundle will come zipped, use the unarchiving utility of your choice to unzip the file.

Once unzipped you will have a folder named apiproxy, this folder contains all of the configuration for your proxy and the folder structure needed for re-importing of the configuration.

The composition of the folder can be described as below.

{ApiName}.xml - A file that contains descriptors for the content

policies - A folder that contains xml policies

proxies - A folder that contains information about your proxy configurations (inbound)

targets - A folder that contains information about target configurations (outbound)

resources - A folder that contains any scripts (java, jsc, py, node)

Note: when creating scripts, place your script/jar files in the proper folders based on the script type (e.g. javascript in jsc, node.js in node, java in java).



### Step 2 Create and configure pom files

In a standard configuration typically we have parent-pom (pom.xml inside the *parent-pom* directory) and a child pom (pom file at  the peer level as folder *apiproxy*).

Parent-pom: The contents of the parent pom folder will contain a single pom.xml file. This file typically contains most of the configuration of Maven and the plugin, it also contains credentials for the Apigee platform. In case of manual creating a Maven compatible file structure, "parent-pom" directory should be in perr level with customer application folders. Here we configure all the information whch is same acroos multiple apiproxys. Eg: Profile configurations which has the org/env info etc.

#### parent-pom-pom-xml Sample

Refer parent-pom template [parent-pom] (https://github.com/apigee/apigee-deploy-maven-plugin/blob/master/samples/forecastweatherapi-recommended/src/gateway/shared-pom.xml)

**groupId** element's content should be set to client's company name.  Here you see it as apigee.

**artifactId** element's content be left as parent-pom.

Child-pom: Here we configure all the details specific to the particular proxy.

#### pom-xml Sample

Refer child-pom template [child-pom](https://github.com/apigee/apigee-deploy-maven-plugin/blob/master/samples/forecastweatherapi-recommended/src/gateway/forecastweatherapi/pom.xml).

**groupId** element's content should match that of the same element in the parent pom.xml.

**artifactId** element's content should be a unique name, typically set to the name of the API.

**name** element's content should match the artifactId above (typically set to the name of the API).

**side-note** groupId and artifactId, combined, define the artifact living quarters within a repository.

### Step 3 Create and configure config-json

The config.json acts as your build time configuration modification descriptor. The file's root object is called configurations, configurations is an array of proxy configurations scoped to an environment. Note: it is important that the name of the configurations match the name of the profiles in the parent-pom.

For instance in the example below you have two configurations one for the test profile and one for the production profile. This example also shows how you can use xpath to replace environment specific settings.



#### Config-json Sample

Refer config.json template [config.json] (https://github.com/apigee/apigee-deploy-maven-plugin/blob/master/samples/forecastweatherapi-recommended/src/gateway/forecastweatherapi/config.json)

     **configuration** - an array of API definitions

        **name** - Name of the maven profile

        **proxies** - Array of proxy definitions, directly correlates to the proxies folder in your API Bundle

           **name** - Name of file to configure

           **tokens** - Array of Actions to Invoke on Elements

              **xpath** - Path to element that your want to change the value of

              **value** - The replacement value

        **policies** - Array of proxy definitions, directly correlates to the policies or step definition folder in your API Bundle

           **name** - Name of file to configure

           **tokens** - Array of Actions to Invoke on Elements

              **xpath** - Path to element that your want to change the value of

              **value** - The replacement value

        **targets** - Array of proxy definitions, directly correlates to the proxies folder in your API Bundle

           **name** - Name of file to configure

           **tokens** - Array of Actions to Invoke on Elements

              **xpath** - Path to element that your want to change the value of

              **value** - The replacement value


Deploy and run the scripts



## Commands for deploying the proxy using maven

*To deploy the proxy*

/src/gateway/proxy-dir
(run the command from the directory same as child pom)

**mvn apigee-enterprise:deploy -P -Dusername=<username> -Dpassword=<password>**

For example:

mvn apigee-enterprise:deploy -P prod -Dusername=admin@toopowerful.com -Dpassword=too\_powerful\_password

*To deploy the proxy and run jmeter tests*

**mvn install -P  <profile_name> -Dusername=<username> -Dpassword=<password>**



## Advanced Configuration Options

**Note 1:** The following entries in some XML file elements could be changed to match the customer's environment: "groupId", "id" (for each profile sections), "apigee.profile", "apigee.env", "apigee.hosturl", "apigee.org". The contents of "apigee.profile", "apigee.env", and "id" elements should match the profile the customer wants to use and is matched with environment name. The value of the "apigee.hosturl" element should match the value in the example if the customer is an enterprise cloud user. If the customer is an on premise user, this url would be the location of the customer management servers host and port. The port is 8080 by default. The value of the "apigee.org" element should match the organization provided when Customer environment was initially setup, in most cases this includes the name of the company. For on premise installations, the org is setup when you run installation scripts. The Maven group id is malleable and is also marked in red for both pom examples, the only thing you should note when changing this is that they need to be consistent between applications.

**Note 2:** The"apigee.override.delay", "apigee.delay,apigee.options" are optional elements. The "apigee.override.delay" could be specified (in milliseconds). This will ensure to add a delay between the operations like delete, import, activate, deactivate etc.

**Note 3:** The "apigee.options" element can have the following values: **clean** (this option will delete the last deployed revision in an environment), **validate** (this option will validate a bundle before importing. Thus if you want strict validation then its required), **inactive** (this option will import the bundle without activating the bundle), **override** (this option is used for seamless deployment and should be supplied with apigee.override.delay parameter. The apigee.override.delay expects delay to be given in seconds), **update** (this option will update the revision). This is similar to import with validation but no new revision is created. If there are any errors in the bundle, an error is thrown and the existing bundle is left intact. In case the revision they are trying to update is deployed, it will internally trigger undeployment and deployment. It is completely in the background and not visible in the response.

**Note3a** . The “apigee.revision” element can be used **when using the update option only**. The update option will be executed on the provided revision.


**Note 4:** The "apigee.options" combination could be given with comma separated values. The precedence order of options are -> override, update, (clean, inactive, validate, force).

**Note 5:** Flow without "apigee.options":import –> undeploy (lastactive) –> deploy (new revision)


## Deploying API Proxies with Node.js apps

Starting at version 1.0.1 of the plugin, support for API proxies that contain node.js applications is included.  The plugin 
will compress the ```node_modules``` directory contained in ```apiproxy/resources/node```.  

The plugin also supports moving node.js application source into ```apiproxy/resources/node``` if the source exists
outside of the API proxy structure itself.  An example structure when node.js application source is beside ```apiproxy```:


    |--name-of-root (typically proxy name)
      |---apiproxy
        |-----proxies
        |-----resources
          |-------node (where root/node gets moved to)
        |-----targets
      |---node (where node.js application source exists)
 
 Note: In above example, if you have code in ```node/``` and ```apiproxy/resources/node```, the source in ```node/``` takes 
 precedence and any files located in ```apiproxy/resource/node``` will be overwritten.

The above structure follows the same pattern when developing with java source code outside of the ```apiproxy``` bundle working directory.

----------------------------------------------------------------
For the users migrating from Apigee Maven repo to Maven central
----------------------------------------------------------------

The plugin was hosted in Apigee Maven repo and is now moved to Maven central for public consumption. We advise all the existing users to move to the new repo for latest updates and enhancements.
(**Repo Apigee URL: ** http://repo.apigee.com:8081/artifactory/repo)

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

The plugin is initially developed by [Santany Dey] (sdey@apigee.com). With major contributions from [Rajesh Mishra] (rajesh.mishra@apigee.com), [Manoj Wartikar] (manojwartikar@apigee.com), [Srikanth Seshadri] (sseshadri@apigee.com) and others listed in the pom developer list are the active developers who can be contacted for support. The plugin is open sourced by [Priyanky Thomas] (priyanky@apigee.com).
