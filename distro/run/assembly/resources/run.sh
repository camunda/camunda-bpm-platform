#!/bin/bash

BASEDIR=$(dirname "$0")
PARENTDIR=$(builtin cd $BASEDIR/..; pwd)
deploymentDir=$PARENTDIR/configuration/resources

# set environment parameters
webappsPath=$BASEDIR/webapps/
restPath=$BASEDIR/rest/
swaggerPath=$BASEDIR/swaggerui
examplePath=$BASEDIR/example
classPath=$PARENTDIR/configuration/userlib/,$PARENTDIR/configuration/keystore/
optionalComponentChosen=false
restChosen=false
swaggeruiChosen=false
productionChosen=false
detachProcess=false
configuration=$PARENTDIR/configuration/default.yml
pidPath=$BASEDIR/run.pid

if [ "$1" = "start" ] ; then
  shift
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
      --production ) configuration=$PARENTDIR/configuration/production.yml
                     productionChosen=true
                     ;;
      # the background flag shouldn't influence the optional component flags
      --detached )   detachProcess=true
                     echo Camunda Run will start in the background. Use the shutdown.sh script to stop it
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
  if [ "$detachProcess" = "true" ]; then

    # check if a Camunda Run instance is already in operation
    if [ -s "$pidPath" ]; then
      echo "
A Camunda Run instance is already in operation (process id $(cat $pidPath)).

Please stop it or remove the file $pidPath."
      exit 1
    fi

    # start Camunda Run detached
    "$JAVA" -Dloader.path="$classPath" -Dcamunda.deploymentDir="$deploymentDir" $JAVA_OPTS -jar "$BASEDIR/camunda-bpm-run-core.jar" --spring.config.location=file:"$configuration" &
    # store the process id
    echo $! > $pidPath

    # open a browser
    UNAME=`which uname`
    if [ -n "$UNAME" -a "`$UNAME`" = "Darwin" ]
    then
      BROWSERS="open"
    else
      BROWSERS="xdg-open gnome-www-browser x-www-browser firefox chromium chromium-browser google-chrome"
    fi

    if [ -z "$BROWSER" ]; then
      for executable in $BROWSERS; do
        BROWSER=`which $executable 2> /dev/null`
        if [ -n "$BROWSER" ]; then
          break;
        fi
      done
    fi

    if [ -z "$BROWSER" ]; then
      (sleep 15; echo -e "We are sorry... We tried all we could do but we couldn't locate your default browser... \nIf you want to see our default website please open your browser and insert this URL:\nhttp://localhost:8080/camunda-welcome/index.html";) &
    else
      (sleep 15; $BROWSER "http://localhost:8080/";) &
    fi

  else
    "$JAVA" -Dloader.path="$classPath" -Dcamunda.deploymentDir="$deploymentDir" $JAVA_OPTS -jar "$BASEDIR/camunda-bpm-run-core.jar" --spring.config.location=file:"$configuration"
  fi

elif [ "$1" = "stop" ] ; then

  if [ -s "$pidPath" ]; then
    # stop Camunda Run if the process is still running
    kill $(cat $pidPath)

    # remove process ID file
    rm $pidPath

    echo "Camunda Run is shutting down."
  else
    echo "There is no instance of Camunda Run to shut down."
    exit 1
  fi

elif [ "$1" = "" ] || [ "$1" = "help" ] ; then

  echo "
Usage: run.sh [start|stop] (options...)
Options:
  --webapps    - Enables the Camunda Platform Webapps
  --rest       - Enables the REST API
  --swaggerui  - Enables the Swagger UI
  --example    - Enables the example application
  --production - Applies the production.yaml configuration file
  --detached   - Starts Camunda Run as a detached process
  "
fi