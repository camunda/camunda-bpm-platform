@echo off

echo "starting camunda BPM ${project.version} on Glassfish Application Server ${version.glassfish}"

cd server\glassfish3\glassfish\bin\
start startserv.bat

ping -n 15 localhost > NULL
start http://localhost:8080/camunda-welcome/index.html
 