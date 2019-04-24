@echo off

set "JBOSS_HOME=%CD%\server\jboss-as-${version.jboss.as}"

setlocal

for /f "tokens=3" %%g in ('java -version 2^>^&1 ^| findstr /i "version"') do (
    set JAVAVER=%%g
)
set JAVAVER=%JAVAVER:"=%
@echo Output: %JAVAVER%

for /f "delims=. tokens=1-3" %%v in ("%JAVAVER%") do (
    if %%w GEQ 8 (
        @echo Your java version is greater than JDK 1.7. Cannot start JBoss AS ${version.jboss.as}
        Exit /B 5
    )
)

endlocal

echo "starting camunda BPM platform ${project.version} on JBoss Application Server ${version.jboss.as}"

cd server\jboss-as-${version.jboss.as}\bin\
start standalone.bat

ping -n 5 localhost > NULL
start http://localhost:8080/camunda-welcome/index.html
