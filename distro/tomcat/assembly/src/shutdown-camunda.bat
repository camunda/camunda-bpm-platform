@echo off

set "CATALINA_HOME=%CD%\server\apache-tomcat-${version.tomcat}"

cd server\apache-tomcat-${version.tomcat}\bin\
start shutdown.bat
