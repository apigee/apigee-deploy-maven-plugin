@echo off

set AST_HOME=%~dp0\..

java -Dast.home="%AST_HOME%" -Dlogback.configurationFile="%AST_HOME%\conf\logback.xml" -classpath "%AST_HOME%\lib\*;%AST_HOME%\ruby-lib" com.apigee.ast.Main %*


