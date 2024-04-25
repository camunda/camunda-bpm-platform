#!/bin/sh

export CATALINA_HOME="$(dirname "$0")/server/apache-tomcat-${version.tomcat9}"

/bin/sh "$(dirname "$0")/server/apache-tomcat-${version.tomcat9}/bin/shutdown.sh"
