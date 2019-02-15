#!/bin/sh

export JBOSS_HOME="$(dirname "$0")/server/jboss-as-${version.jboss.as}"

UNAME=`which uname`
if [ -n "$UNAME" -a "`$UNAME`" = "Darwin" ]
then
	BROWSERS="open"
else
	BROWSERS="xdg-open gnome-www-browser x-www-browser firefox chromium chromium-browser google-chrome"
fi

if type -p java; then
    echo "found java executable in PATH"
    _java=java
elif [[ -n "$JAVA_HOME" ]] && [[ -x "$JAVA_HOME/bin/java" ]];  then
    echo "found java executable in JAVA_HOME"
    _java="$JAVA_HOME/bin/java"
else
    echo "no java found"
fi

if [[ "$_java" ]]; then
    version=$("$_java" -version 2>&1 | awk -F '"' '/version/ {print $2}')
    echo "version $version"
    if ! [[ "$version" < "1.8" ]]; then
        echo "Your java version is greater than JDK 1.7. Cannot start JBoss AS ${version.jboss.as}"
        exit 1;
    fi
fi

echo "starting camunda BPM ${project.version}  on JBoss Application Server ${version.jboss.as}";

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
  (sleep 15; $BROWSER "http://localhost:8080/camunda-welcome/index.html";) &
fi

/bin/sh "$(dirname "$0")/server/jboss-as-${version.jboss.as}/bin/standalone.sh"
