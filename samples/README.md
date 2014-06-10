## Getting Started
- [ ] [Sign up for an Apigee Account!](https://accounts.apigee.com/accounts/sign_up?callback=https://enterprise.apigee.co). Not required if already provided.
- [ ] [Download and install Maven 3.0.*](http://maven.apache.org/download.cgi)
- [ ] Clone this repo
- [ ] ```cd {sample-apiproxy-above}``` 
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

## More Documentation
* [Ask The Expert Presentation](https://community.apigee.com/learn/know-ask-expert-and-office-hours)
