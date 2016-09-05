## Getting Started
- [ ] [Sign up for an Apigee Account!](https://accounts.apigee.com/accounts/sign_up?callback=https://enterprise.apigee.co). Not required if already provided.
- [ ] [Download and install Maven 3.0.*](http://maven.apache.org/download.cgi)
- [ ] Clone this repo https://github.com/apigee/apigee-deploy-maven-plugin
- [ ] ```cd forecastweatherapi-recommended/src/gateway/forecastweatherapi``` **Recommended template
- [ ] Execute ```mvn install -Ptest -Dusername={apigee-edge-email} -Dpassword={apigee-edge-password} -Dorg={apigee-edge-org}```

That's it! If everything ran smooth, you will see BUILD SUCCESS message at the of the execution of this command. Next steps, learn a bit of Maven to customize the pom.xml.

##Basic Commands – apigee.options

###Configure, package, import, deploy, and test bundle (default validate apigee.option) – Creates new revision

```mvn install -Ptest -Dusername=$ae_username -Dpassword=$ae_password -Dorg=testmyapi```

###Configure, package, import, override, deploy, and test bundle (default validate apigee.option) – Overrides current revision

```mvn install -Ptest -Dusername=$ae_username -Dpassword=$ae_password -Doptions=validate,update```

###Delete current bundle deployed

```mvn install -Ptest -Dusername=$ae_username -Dpassword=$ae_password -Doptions=clean```

```mvn install -Ptest -Dusername=$ae_username -Dpassword=$ae_password -Dorg=testmyapi -Doptions=inactive```

###Configure and package bundle. Does not import

```mvn package -Ptest -Dusername=$ae_username -Dpassword=$ae_password -Dorg=testmyapi```

###Run tests only

```mvn jmeter:jmeter -Ptest -Dusername=$ae_username -Dpassword=$ae_password -Dorg=testmyapi -DtestData=weather_test.csv -DthreadNum=5 -DrampUpPeriodSecs=5 -DloopCount=2```

##MFA - Optional
Apigee protects its management APIs using OAuth tokens as an alternative to the 
Basic Auth security. Additionally MFA using TOTP can also be configured as an 
additional layer of security for acquiring OAuth tokens. The plugin has the 
capability to acquire OAuth tokens and invoke management API calls.

The following parameters can be used to configure OAuth token acquistion.
### OAuth token (defaults to cloud version)
```mvn install -Ptest -Dusername=$ae_username -Dpassword=$ae_password -Dorg=testmyapi -Dmgmttokenurl='https://login.apigee.com/oauth/token'```

### OAuth token with MFA
```mvn install -Ptest -Dusername=$ae_username -Dpassword=$ae_password -Dorg=testmyapi -Dmgmttokenurl='https://login.apigee.com/oauth/token' -Dmfatoken=$mfa_token```
Refer to [How to get OAuth2 tokens](http://docs.apigee.com/api-services/content/using-oauth2-security-apigee-edge-management-api#howtogetoauth2tokens) for details
mfatoken.js provides an example.

###The following are available options:
a. clean - This will delete the last deployed revision in an environment.

b. validate - This will validate a bundle before importing. Thus if you want strict validation then its required.

c. inactive - This will just import the bundle without activating the bundle.

d. override - This is used for seamless deployment. This must be supplied with apigee.override.delay parameter. The apigee.override.delay expects delay to be given in seconds.

e. force - This will recheck the un deployment of bundle before proceeding further deployment.

f. update - It will update the deployed revision .  This is similar to import with validation but no new revision is created. If there any errors in the bundle, error is thrown and the existing bundle is left intact. In case the revision they are trying to update is deployed, it will internally trigger undeployment and deployment. It is completely in the background and not visible in the response. It is advised not to update the deployed revision. (UI could show a warning or something in this case).

## More Documentation
* [Ask The Expert Presentation](https://community.apigee.com/learn/know-ask-expert-and-office-hours)
