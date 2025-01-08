#!/bin/bash

# set constants
BASEDIR=$(dirname "$0")
PARENTDIR=$(builtin cd "$BASEDIR/.."; pwd)
DEPLOYMENT_DIR=$PARENTDIR/configuration/resources
WEBAPPS_PATH=$BASEDIR/webapps/
OAUTH2_PATH=$BASEDIR/oauth2/
REST_PATH=$BASEDIR/rest/
EXAMPLE_PATH=$BASEDIR/example
PID_PATH=$BASEDIR/run.pid
OPTIONS_HELP="Options:
  --webapps    - Enables the Camunda Platform Webapps
  --oauth2     - Enables the Camunda Platform Spring Security OAuth2 integration
  --rest       - Enables the REST API
  --example    - Enables the example application
  --production - Applies the production.yaml configuration file
  --detached   - Starts Camunda Run as a detached process
"

# set environment parameters
optionalComponentChosen=false
restChosen=false
productionChosen=false
detachProcess=false
classPath=$PARENTDIR/configuration/userlib/,$PARENTDIR/configuration/keystore/
configuration=$PARENTDIR/configuration/default.yml

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

  EXPECTED_JAVA_VERSION=17
  JAVA_VERSION=$("$JAVA" -version 2>&1 | head -1 | cut -d'"' -f2 | sed '/^0\./s///' | cut -d'.' -f1)
  echo Java version is $("$JAVA" -version 2>&1 | head -1 | cut -d'"' -f2)
  if [[ "$JAVA_VERSION" -lt "$EXPECTED_JAVA_VERSION" ]]; then
    echo You must use at least JDK 17 to start Camunda Platform Run.
    exit 1
  fi

  if [ "x$JAVA_OPTS" != "x" ]; then
    echo JAVA_OPTS: $JAVA_OPTS
  fi

  # inspect arguments
  while [ "$1" != "" ]; do
    case $1 in
      --webapps )    optionalComponentChosen=true
                     classPath=$WEBAPPS_PATH,$classPath
                     echo WebApps enabled
                     ;;
      --oauth2 )     optionalComponentChosen=true
                     classPath=$OAUTH2_PATH,$classPath
                     echo Spring Security OAuth2 enabled
                     ;;
      --rest )       optionalComponentChosen=true
                     restChosen=true
                     classPath=$REST_PATH,$classPath
                     echo REST API enabled
                     ;;
      --example )    optionalComponentChosen=true
                     classPath=$EXAMPLE_PATH,$classPath
                     echo Invoice Example included - needs to be enabled in application configuration as well
                     ;;
      --production ) configuration=$PARENTDIR/configuration/production.yml
                     productionChosen=true
                     ;;
      # the background flag shouldn't influence the optional component flags
      --detached )   detachProcess=true
                     echo Camunda Run will start in the background. Use the shutdown.sh script to stop it
                     ;;
      --help )       printf "%s" "$OPTIONS_HELP"
                     exit 0
                     ;;
      * )            printf "Unexpected argument '%s'!" "$1"
                     printf "%s" "$OPTIONS_HELP"
                     exit 1
                     ;;
    esac
    shift
  done

  # If no optional component is chosen, enable REST and Webapps.
  # If production mode is not chosen, also enable the example application.
  if [ "$optionalComponentChosen" = "false" ]; then
    restChosen=true
    echo REST API enabled
    echo WebApps enabled
    if [ "$productionChosen" = "false" ]; then
      echo Invoice Example included - needs to be enabled in application configuration as well
      classPath=$EXAMPLE_PATH,$classPath
    fi
    classPath=$WEBAPPS_PATH,$REST_PATH,$classPath
  fi

  echo classpath: $classPath

  # start the application
  if [ "$detachProcess" = "true" ]; then

    # check if a Camunda Run instance is already in operation
    if [ -s "$PID_PATH" ]; then
      echo "
A Camunda Run instance is already in operation (process id $(cat $PID_PATH)).

Please stop it or remove the file $PID_PATH."
      exit 1
    fi

    # start Camunda Run detached
    "$JAVA" -Dloader.path="$classPath" -Dcamunda.deploymentDir="$DEPLOYMENT_DIR" $JAVA_OPTS -jar "$BASEDIR/camunda-bpm-run-core.jar" --spring.config.location=file:"$configuration" &
    # store the process id
    echo $! > "$PID_PATH"

  else
    "$JAVA" -Dloader.path="$classPath" -Dcamunda.deploymentDir="$DEPLOYMENT_DIR" $JAVA_OPTS -jar "$BASEDIR/camunda-bpm-run-core.jar" --spring.config.location=file:"$configuration"
  fi

elif [ "$1" = "stop" ] ; then

  if [ -s "$PID_PATH" ]; then
    # stop Camunda Run if the process is still running
    kill $(cat "$PID_PATH")

    # remove process ID file
    rm "$PID_PATH"

    echo "Camunda Run is shutting down."
  else
    echo "There is no instance of Camunda Run to shut down."
    exit 1
  fi

elif [ "$1" = "" ] || [ "$1" = "help" ] ; then

  printf "Usage: run.sh [start|stop] (options...) \n%s" "$OPTIONS_HELP"
fi