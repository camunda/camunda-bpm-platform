@echo off

echo "starting camunda BPM platform ${project.version} on Apache Tomcat ${tomcat.version}"

cd server\apache-tomcat-${tomcat.version}\bin\
start startup.bat

ping -n 5 localhost > NULL
start http://localhost:8080/camunda-welcome/index.html
 