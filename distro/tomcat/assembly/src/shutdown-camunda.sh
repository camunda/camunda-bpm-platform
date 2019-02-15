#!/bin/sh

export CATALINA_HOME="$(dirname "$0")/server/apache-tomcat-${version.tomcat}"

/bin/sh "$(dirname "$0")/server/apache-tomcat-${version.tomcat}/bin/shutdown.sh"
