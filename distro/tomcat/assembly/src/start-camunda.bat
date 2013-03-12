@echo off

echo "starting camunda BPM platform ${project.version} on Apache Tomcat ${tomcat.version}"

cd server\apache-tomcat-${tomcat.version}\bin\
startup.bat

start http://localhost:8080/
 