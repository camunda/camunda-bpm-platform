@echo off

set "CATALINA_HOME=%CD%\server\apache-tomcat-${version.tomcat}"

echo "starting Camunda Platform ${project.version} on Apache Tomcat ${version.tomcat}"

cd server\apache-tomcat-${version.tomcat}\bin\
start startup.bat

ping -n 5 localhost > NULL
start http://localhost:8080/camunda-welcome/index.html
 