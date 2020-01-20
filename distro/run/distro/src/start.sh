#!/bin/bash


#### Usage

usage()
{
  echo "usage: start.sh [--webapps | --rest]+"
}


#### Main

webappsPath=../lib/webapps/
restPath=../lib/rest/
classPath=../lib/db/

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

java -Dloader.path="$classPath" -jar "../lib/camunda-bpm-rest-distro-1.0-SNAPSHOT.jar" --spring.config.location=file:../config/application.yml