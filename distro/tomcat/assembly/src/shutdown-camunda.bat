@echo off

set "CATALINA_HOME=%CD%\server\apache-tomcat-${version.tomcat9}"

cd server\apache-tomcat-${version.tomcat9}\bin\
start shutdown.bat
