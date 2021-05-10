## Getting Started
- [Sign up for an Apigee Hybrid Account!](https://apigee.google.com)
- [Download and install Maven 3.*](http://maven.apache.org/download.cgi)
- For API Proxy - ```cd samples/mockapi-recommended/src/gateway/Mock-v1``` *Recommended template*
- For sharedflow - ```cd samples/security-sharedflow/src/sharedflows/security``` *Recommended template*
- Execute ```mvn install -Ptest -Dorg={apigee-org} -Denv={apigee-env} -Dfile={path-to-service_account_file}```
	or
	```mvn install -Ptest -Dorg={apigee-org} -Denv={apigee-env} -Dbearer={accesstoken}```
	
For passing bearer token (v2.0.1 or later), you can just run (with gcloud sdk installed on your machine)
`mvn clean install -Ptest -Dbearer=$(gcloud auth print-access-token)`

NOTE: If you pass both bearer and service account file, the bearer token will take precedence

That's it! If everything ran smooth, you will see BUILD SUCCESS message at the of the execution of this command. Next steps, learn a bit of Maven to customize the pom.xml.

## Basic Commands – apigee.options

### Configure, package, import, deploy, and test bundle – Creates new revision

`mvn install -Ptest -Dorg=$ae_org -Denv=$ae_env -Dfile=$path-to-service_account_file`

### The following are available options:
a. override - This is used for seamless deployment
