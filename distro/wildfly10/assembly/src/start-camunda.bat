@echo off

set "JBOSS_HOME=%CD%\server\wildfly-${version.wildfly10}"

echo "starting camunda BPM platform ${project.version} on Wildfly Application Server ${version.wildfly10}"

cd server\wildfly-${version.wildfly10}\bin\
start standalone.bat

ping -n 5 localhost > NULL
start http://localhost:8080/camunda-welcome/index.html
 