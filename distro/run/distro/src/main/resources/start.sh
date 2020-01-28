#!/bin/bash

BASEDIR=$(dirname "$0")

#### Usage

usage()
{
  echo "usage: start.sh [--webapps | --rest]+"
}


#### Main

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
webappsPath=$BASEDIR/../lib/webapps/
restPath=$BASEDIR/../lib/rest/
classPath=$BASEDIR/../lib/db/


# inspect arguments
while [ "$1" != "" ]; do
  case $1 in 
    --webapps ) classPath=$webappsPath,$classPath
                echo WebApps enabled
                ;;
    --rest )    classPath=$restPath,$classPath
                echo REST API enabled
                ;;
    * )         usage
                exit 1
  esac
  shift
done

echo classpath: $classPath

# start the application
"$JAVA" -Dloader.path="$classPath" -jar "$BASEDIR/../lib/camunda-rest-distro.jar" --spring.config.location=file:"$BASEDIR"/../config/application.yml