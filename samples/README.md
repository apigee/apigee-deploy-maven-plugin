## Getting Started
- [Sign up for an Apigee Hybrid Account!](https://apigee.google.com)
- [Download and install Maven 3.*](http://maven.apache.org/download.cgi)
- For API Proxy - ```cd samples/mockapi-recommended/src/gateway/Mock-v1``` *Recommended template*
- For sharedflow - ```cd samples/security-sharedflow/src/sharedflows/security``` *Recommended template*
- Execute ```mvn install -Ptest -Dusername={apigee-edge-email} -Dpassword={apigee-edge-password} -Dorg={apigee-edge-org}```

That's it! If everything ran smooth, you will see BUILD SUCCESS message at the of the execution of this command. Next steps, learn a bit of Maven to customize the pom.xml.

## Basic Commands – apigee.options

### Configure, package, import, deploy, and test bundle (default validate apigee.option) – Creates new revision

`mvn install -Ptest -Dusername=$ae_username -Dpassword=$ae_password -Dorg=testmyapi`

### The following are available options:
a. override - This is used for seamless deployment. This must be supplied with apigee.override.delay parameter. The apigee.override.delay expects delay to be given in seconds.
