----------------------------------------------------------------
apigee-deploy-maven-plugin (Apigee X or Apigee hybrid)
-----------------------------------------------------------

apigee-edge-maven-plugin is a build and deploy utility for building and deploying the Apigee ApiProxy's/Application bundles into Apigee hybrid Edge Platform.
The code is distributed under the Apache License 2.0.

**NOTE:** Log4J libraries are upgraded to v2.17.1 in v2.2.2

------------
TL;DR
------------

The [samples folder](https://github.com/apigee/apigee-deploy-maven-plugin/tree/hybrid/samples) provides a Readme with Getting Started steps and commands to hit the ground quickly.

------------
Video
------------

Learn more, check out this video! [Ask the Expert](https://www.youtube.com/watch?v=u7zsQR4e0mE)

-------------------------------------------
## Detailed documentation on the use of plugin
-------------------------------------------

## Contents

- [Prerequisites](#Prerequisites)
- [Getting-Started](#getting-started)
- [Building API bundles](#building-api-bundles)
- [Building Shared Flow bundles](#building-shared-flow-bundles)
- [Steps to set it up](#steps-to-set-it-up)
  - [Step 1: Create a maven compatible file structure](#step-1-create-a-maven-compatible-file-structure)
  - [Step 2: Create and configure pom files](#step-2-create-and-configure-pom-files)
  - [Step 3: Create and configure config.json](#step-3-create-and-configure-config-json)
- Samples
  - [parent-pom/pom.xml Sample](#parent-pom-pom-xml-sample)
  - [pom.xml Sample](#pom-xml-sample)
  - [Config.json Sample](#config-json-sample)
- [Commands](#commands-for-deploying-the-proxy-using-maven)
- [To configure a proxy](#to-configure-a-proxy)

# Prerequisites
You will need the following to run the samples:
- Apigee Edge developer account. See [docs](http://apigee.google.com) for more details on how to setup your account..
- [Java SDK >= 8](http://www.oracle.com/technetwork/java/javase/downloads/index.html)
- [Maven 3.x](https://maven.apache.org/)

## Plugin Usage

### NOTE ###
- If you want to use this plugin for Apigee SaaS/Private Cloud, please refer to this [link](https://github.com/apigee/apigee-deploy-maven-plugin). You should be using the version 1.x
```xml
<dependency>
  <groupId>io.apigee.build-tools.enterprise4g</groupId>
  <artifactId>apigee-edge-maven-plugin</artifactId>
  <version>1.x</version>
</dependency>
```
- For Apigee X or Apigee hybrid, the version of the plugin is 2.x
```xml
<dependency>
  <groupId>io.apigee.build-tools.enterprise4g</groupId>
  <artifactId>apigee-edge-maven-plugin</artifactId>
  <version>2.x</version>
</dependency>
```

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

```
 |-apiproxy/
   |-proxies
   |-resources
   |-policies
   |-targets
```

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

### Step 1 Create a Maven compatible file structure


Below is the recommended structure for the project. However only the folder structure below the folder gateway is mandatory

```
  |-name-of-root (typically company name)
    |-archive/
      |-docs
      |-src
    |-binaries/
    |-build-artifacts/
    |-docs/
      |-customer
      |-reference
      |-solution
    |-src/
      |-analytics
      |-gateway/ ***
        |-parent-pom
        |-test-app/
          |-apiproxy/
            |-proxies
            |-resources/
              |-py
            |-policies
            |-targets
      |-java
      |-portal
    |-test
```

#### Decompress API Bundle

The API bundle will come zipped, use the unarchiving utility of your choice to unzip the file.

Once unzipped you will have a folder named apiproxy, this folder contains all of the configuration for your proxy and the folder structure needed for re-importing of the configuration.

The composition of the folder can be described as below.

File/Folder | Purpose
---- | ----
{ApiName}.xml | A file that contains descriptors for the content
policies/ | A folder that contains all policy xml files
proxies/ | A folder that contains information about your proxy configurations (inbound)
targets/ | A folder that contains information about target configurations (outbound)
resources | A folder that contains any scripts (java, jsc, py, node)

Note: when creating scripts, place your script/jar files in the proper folders based on the script type (e.g. JavaScript in jsc, node.js in node, java in java).



### Step 2 Create and configure pom files

In a standard configuration typically we have parent-pom (pom.xml inside the *parent-pom* directory) and a child pom (pom file at  the peer level as folder *apiproxy*).

The contents of the parent pom folder will contain a single pom.xml file. This file typically contains most of the configuration of Maven and the plugin, it also contains credentials for the Apigee platform.

In case of manual creation of Maven compatible file structure, "parent-pom" directory should be in peer level with other application folders. Here we configure information that is common across multiple apiproxys. Eg: Profile configurations which has the org/env info etc.

#### parent-pom-pom-xml Sample

Refer parent-pom template [parent-pom](./samples/mockapi-recommended/src/gateway/shared-pom.xml)

 * **groupId** element's content should be set to client's company name.  Here you see it as apigee.
 * **artifactId** element's content be left as parent-pom.

Child-pom: Here we configure all the details specific to the particular proxy.

#### pom-xml Sample

Refer child-pom template [child-pom](./samples/mockapi-recommended/src/gateway/Mock-v1/pom.xml).

 * **groupId** element's content should match that of the same element in the parent pom.xml.
 * **artifactId** element's content should be a unique name, typically set to the name of the API.
 * **name** element's content should match the artifactId above (typically set to the name of the API).
 * **side-note** groupId and artifactId, combined, define the artifact living quarters within a repository.

### Step 3 Create and configure config-json

The config.json contains rules to perform build time configuration update. This JSON file's root object is  "configurations" and is an array of proxy configurations scoped to an environment. 

Note: it is important that the name of the configurations match the name of the profiles in the parent-pom.

For instance in the example below you have two configurations one for the test profile and one for the production profile. This example also shows how you can use xpath to replace environment specific settings.

#### Config-json Sample

Refer config.json template [config.json](./samples/mockapi-recommended/src/gateway/Mock-v1/config.json)

## Commands for deploying the proxy using maven

### To deploy the proxy

```
/src/gateway/proxy-dir
(run the command from the directory same as child pom)

mvn apigee-enterprise:deploy -P<profile> -Dfile={file}
```
The default configuration is "override" and in this option, the plugin polls to check if the deployment is complete across all pods. If you do not want the plugin to poll, please pass -Dapigee.options=async. This is available in v2.0.2 and later

```
mvn apigee-enterprise:deploy -P<profile> -Dfile={file} -Dapigee.options=async
```

For example:

```
mvn apigee-enterprise:deploy -P prod -Dfile={file}
```

You can also pass the bearer token to deploy (available from v2.0.1 and later)

```
mvn clean install -P{profile} -Dbearer=${bearer} -Dapigee.options={option}
```

For example using gcloud

```
mvn clean install -P{profile} -Dbearer=$(gcloud auth print-access-token) -Dapigee.options={option}
```

### To delete the proxy or sharedflow (v2.0.3 or later)

To delete the entire proxy or sharedflow, pass the options as `clean`

```
mvn clean install -P{profile} -Dbearer=$(gcloud auth print-access-token) -Dapigee.options=clean
```

### To deploy a proxy that makes requires Apigee to generate the GoogleAccessToken or GoogleIDToken (v2.1.2 or later) 

**NOTE: This option is available in Apigee X and Apigee hybrid 1.6+**

If the API Proxy makes a callout to a Google API, Apigee now supports generating the access token or ID Token by just passing the service account email to the deployment API. For more info on the policy and HTTPTargetConnection config, check out [Authentication](https://cloud.google.com/apigee/docs/api-platform/reference/policies/service-callout-policy#authentication). 

```
mvn clean install -P{profile} -Dbearer=$(gcloud auth print-access-token) -DgoogleTokenEmail={ACCOUNT_ID}@{PROJECT}.iam.gserviceaccount.com
```

For more info check out [Using Google authentication using Apigee](https://cloud.google.com/apigee/docs/api-platform/security/google-auth/overview)

**NOTE: Please make sure that you have the following config in your Maven profile**
```
<apigee.googletoken.email>${googleTokenEmail}</apigee.googletoken.email>
```

## Advanced Configuration Options

##### Note 1
The following entries in some XML file elements could be changed to match the customer's environment: 
* "groupId"
* "id" (for each profile sections)
* "apigee.profile"
* "apigee.env"
* "apigee.hosturl"
* "apigee.org"

1. The contents of "apigee.profile", "apigee.env", and "id" elements should match the profile the customer wants to use and is matched with environment name. 
2. The value of the "apigee.hosturl" element should match the value in the example if the customer is an enterprise cloud user. 
    * If the customer is an private cloud user, this url would be the location of the customer's management server host and port. The port is 8080 by default. 
3. The value of the "apigee.org" element should match the organization provided when Customer environment was initially setup, in most cases this includes the name of the company. 
   * For private cloud installations, the org is setup when you run installation scripts. The Maven group id is malleable and is also marked in red for both pom examples, the only thing to note when changing this is that they need to be consistent between applications.

## Building Shared Flow bundles

### What is a Shared Flow bundle?

Shared Flow bundles can be described as a zipped file system composed of policies, steps and code. The file system when extracted is composed of the following structure.

```
 |-sharedflowbundle/
   |-policies
   |-sharedflows
```
The build steps and the options available for building and deploying Shared Flows are the same as API Proxy.
The [samples](./samples/security-sharedflow/src/sharedflows) has an example of a standard sharedflow with the folder structure and the parent pom file. The only key difference between the API Proxy and the Shared Flow is a new property as part of the profiles.

`<apigee.apitype>sharedflow</apigee.apitype>`

This is required to differentiate the build and deployment process.

## To configure a proxy 
Supported from v2.3.0

Please refer to this [doc](http://maven.apache.org/guides/mini/guide-proxies.html) that explains how to setup proxy settings in your settings.xml usually in your $HOME/.m2 directory. Only `https` protocol is supported

------------------------------------------
Recommended Convention for Contributions
------------------------------------------

Refer [Guide for Plugin Developers](https://github.com/apigee/apigee-deploy-maven-plugin/blob/master/PluginDevelopers-Guide.md)


People Involved
------------------------

The plugin is initially developed by [Santany Dey](sdey@apigee.com). With major contributions from [Sai Saran Vaidyanathan](https://github.com/ssvaidyanathan), [Madhan Sadasivam](https://github.com/msadasivam). The plugin is open sourced by [Priyanky Thomas](priyanky@apigee.com). 

## Support
Issues filed on Github are not subject to service level agreements (SLAs) and responses should be assumed to be on an ad-hoc volunteer basis. The
[Apigee community board](https://community.apigee.com/) is recommended as for community support and is regularly checked by Apigee experts.

Apigee customers should use [formal support channels](https://cloud.google.com/apigee/support) for Apigee product related concerns.

## Disclaimer
This is not an officially supported Google product.
