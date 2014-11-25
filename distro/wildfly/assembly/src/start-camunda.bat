@echo off

set "JBOSS_HOME=%CD%\server\wildfly-${version.wildfly}"

echo "starting camunda BPM platform ${project.version} on Wildfly Application Server ${version.wildfly}"

cd server\wildfly-${version.wildfly}\bin\
start standalone.bat

ping -n 5 localhost > NULL
start http://localhost:8080/camunda-welcome/index.html
 