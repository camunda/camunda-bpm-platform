#!/bin/bash

BASEDIR=$(dirname "$0")
deploymentDir=$BASEDIR/configuration/resources

# setup the JVM
if [ "x$JAVA" = "x" ]; then
  if [ "x$JAVA_HOME" != "x" ]; then
    echo Setting JAVA property to "$JAVA_HOME/bin/java"
    JAVA="$JAVA_HOME/bin/java"
  else
    echo JAVA_HOME is not set. Unexpected results may occur.
    echo Set JAVA_HOME to the directory of your local JDK to avoid this message.
    JAVA="java"
  fi
fi


# set environment parameters
webappsPath=$BASEDIR/internal/webapps/
restPath=$BASEDIR/internal/rest/
swaggerPath=$BASEDIR/internal/swaggerui
classPath=$BASEDIR/configuration/userlib/,$BASEDIR/configuration/keystore/
optionalComponentChosen=false
restEnabled=false
swaggeruiEnabled=false
configuration=$BASEDIR/configuration/default.yml


# inspect arguments
while [ "$1" != "" ]; do
  case $1 in 
    --webapps )    optionalComponentChosen=true
                   classPath=$webappsPath,$classPath
                   echo WebApps enabled
                   ;;
    --rest )       optionalComponentChosen=true
                   restEnabled=true
                   classPath=$restPath,$classPath
                   echo REST API enabled
                   ;;
    --production ) configuration=$BASEDIR/configuration/production.yml
                   ;;
    --swaggerui )  classPath=$swaggerPath,$classPath
                   swaggeruiEnabled=true
                   echo Swagger UI enabled
                   ;;
    * )            exit 1
  esac
  shift
done

# if neither REST nor Webapps are explicitly chosen, enable both
if [ "$optionalComponentChosen" = "false" ]; then
  restEnabled=true
  echo REST API enabled
  echo WebApps enabled
  classPath=$webappsPath,$restPath,$classPath
fi

# if Swagger UI is enabled but REST is not, warn the user
if [ "$swaggeruiEnabled" = "true" ] && [ "$restEnabled" = "false" ]; then
  echo You did not enable the REST API. Swagger UI will not be able to send any requests to this Camunda Platfrom Run instance.
fi

echo classpath: $classPath

# start the application
"$JAVA" -Dloader.path="$classPath" -Dcamunda.deploymentDir="$deploymentDir" -jar "$BASEDIR/internal/camunda-bpm-run-core.jar" --spring.config.location=file:"$configuration"