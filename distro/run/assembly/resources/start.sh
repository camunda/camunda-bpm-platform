#!/bin/bash

BASEDIR=$(dirname "$0")
runScript=$BASEDIR/internal/run.sh

if [ $# -eq 0 ]; then

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

  # check Java version >= 17
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

  # open a browser (must be done first)
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
    (sleep 5; echo -e "We are sorry... We tried all we could do but we couldn't locate your default browser... \nIf you want to see our default website please open your browser and insert this URL:\nhttp://localhost:8080/camunda-welcome/index.html";) &
  else
    (sleep 10; $BROWSER "http://localhost:8080/camunda-welcome/index.html";) &
  fi

  # start Camunda Run in the background
  exec $runScript start --detached

else
  # start Camunda Run with the passed arguments
  exec $runScript start "$@"
fi

