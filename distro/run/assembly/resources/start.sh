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

if [ "x$JAVA_OPTS" != "x" ]; then
  echo JAVA_OPTS: $JAVA_OPTS
fi

# set environment parameters
webappsPath=$BASEDIR/internal/webapps/
restPath=$BASEDIR/internal/rest/
swaggerPath=$BASEDIR/internal/swaggerui
examplePath=$BASEDIR/internal/example
classPath=$BASEDIR/configuration/userlib/,$BASEDIR/configuration/keystore/
optionalComponentChosen=false
restChosen=false
swaggeruiChosen=false
productionChosen=false
configuration=$BASEDIR/configuration/default.yml


# inspect arguments
while [ "$1" != "" ]; do
  case $1 in 
    --webapps )    optionalComponentChosen=true
                   classPath=$webappsPath,$classPath
                   echo WebApps enabled
                   ;;
    --rest )       optionalComponentChosen=true
                   restChosen=true
                   classPath=$restPath,$classPath
                   echo REST API enabled
                   ;;
    --swaggerui )  optionalComponentChosen=true
                   swaggeruiChosen=true
                   classPath=$swaggerPath,$classPath
                   echo Swagger UI enabled
                   ;;
    --example )    optionalComponentChosen=true
                   classPath=$examplePath,$classPath
                   echo Invoice Example included - needs to be enabled in application configuration as well
                   ;;
    --production ) configuration=$BASEDIR/configuration/production.yml
                   productionChosen=true
                   ;;
    * )            exit 1
  esac
  shift
done

# If no optional component is chosen, enable REST and Webapps.
# If production mode is not chosen, also enable Swagger UI and the example application.
if [ "$optionalComponentChosen" = "false" ]; then
  restChosen=true
  echo REST API enabled
  echo WebApps enabled
  if [ "$productionChosen" = "false" ]; then
    swaggeruiChosen=true
    echo Swagger UI enabled
    echo Invoice Example included - needs to be enabled in application configuration as well
    classPath=$swaggerPath,$examplePath,$classPath
  fi
  classPath=$webappsPath,$restPath,$classPath
fi

# if Swagger UI is enabled but REST is not, warn the user
if [ "$swaggeruiChosen" = "true" ] && [ "$restChosen" = "false" ]; then
  echo You did not enable the REST API. Swagger UI will not be able to send any requests to this Camunda Platform Run instance.
fi

echo classpath: $classPath

# start the application
"$JAVA" -Dloader.path="$classPath" -Dcamunda.deploymentDir="$deploymentDir" $JAVA_OPTS -jar "$BASEDIR/internal/camunda-bpm-run-core.jar" --spring.config.location=file:"$configuration"
