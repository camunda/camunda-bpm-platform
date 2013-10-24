@echo off

echo "starting camunda BPM platform ${project.version} on JBoss Application Server ${version.jboss.as}"

cd server\jboss-as-${version.jboss.as}\bin\
start standalone.bat

ping -n 5 localhost > NULL
start http://localhost:8080/camunda-welcome/index.html
 