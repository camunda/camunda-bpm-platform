@echo off

SET APPNAME=Camunda 7 WildFly admin mode
SET NOPAUSE=1
SET "JBOSS_HOME=%CD%\..\server\wildfly-29.0.0.Final"

SET CONFIG=wildfly-config.properties
IF NOT [%~1]==[] (
  SET CONFIG=%1
  SHIFT
)

ECHO starting Wildfly in admin-only mode
START "%APPNAME%" %JBOSS_HOME%\bin\standalone.bat --admin-only

timeout /t 10 /nobreak > NUL

ECHO updating Wildfly configuration
CALL %JBOSS_HOME%\bin\jboss-cli.bat -c --file=wildfly-config.cli --properties=%CONFIG%

ECHO configuration updated, shutting down server
taskkill /FI "WINDOWTITLE eq %APPNAME%" /T /F > NUL

PAUSE