@echo off

set "CATALINA_HOME=%CD%\server\apache-tomcat-${version9.tomcat}"

echo "starting Camunda Platform ${project.version} on Apache Tomcat ${version9.tomcat}"

cd server\apache-tomcat-${version.tomcat9}\bin\
start startup.bat

ping -n 5 localhost > NULL
start http://localhost:8080/camunda-welcome/index.html
 