@echo off

SET BASEDIR=%~dp0
SET PARENTDIR=%BASEDIR%..\
SET DEPLOYMENTDIR=%PARENTDIR%configuration/resources
SET APPNAME=Camunda Run

IF [%~1]==[start] GOTO Startup
IF [%~1]==[stop] GOTO Stop
IF [%~1]==[] GOTO Help

:Startup
REM remove argument
SHIFT

REM setup the JVM
IF "x%JAVA_HOME%" == "x" (
  SET JAVA=java
  ECHO JAVA_HOME is not set. Unexpected results may occur.
  ECHO Set JAVA_HOME to the directory of your local JDK to avoid this message.
) ELSE (
  IF NOT EXIST "%JAVA_HOME%" (
    ECHO JAVA_HOME "%JAVA_HOME%" path doesn't exist
    GOTO :EOF
  ) ELSE (
    IF NOT EXIST "%JAVA_HOME%\bin\java.exe" (
      ECHO "%JAVA_HOME%\bin\java.exe" does not exist
      GOTO :EOF
    )
    ECHO Setting JAVA property to "%JAVA_HOME%\bin\java"
    SET JAVA="%JAVA_HOME%\bin\java"
  )
)

IF NOT "x%JAVA_OPTS%" == "x" (
  ECHO JAVA_OPTS: %JAVA_OPTS%
)

REM set environment parameters
SET webappsPath=%BASEDIR%webapps
SET restPath=%BASEDIR%rest
SET swaggerPath=%BASEDIR%swaggerui
SET examplePath=%BASEDIR%example
SET classPath=%PARENTDIR%configuration\userlib,%PARENTDIR%configuration\keystore
SET optionalComponentChosen=false
SET restChosen=false
SET swaggeruiChosen=false
SET productionChosen=false
SET detachProcess=false
SET configuration=%PARENTDIR%configuration\default.yml


REM inspect arguments
:Loop
IF [%~1]==[] GOTO Continue

IF [%~1]==[--webapps] (
  SET optionalComponentChosen=true
  SET classPath=%webappsPath%,%classPath%
  ECHO WebApps enabled
)

IF [%~1]==[--rest] (
  SET optionalComponentChosen=true
  SET restChosen=true
  SET classPath=%restPath%,%classPath%
  ECHO REST API enabled
)

IF [%~1]==[--swaggerui] (
  SET optionalComponentChosen=true
  SET swaggeruiChosen=true
  SET classPath=%swaggerPath%,%classPath%
  ECHO Swagger UI enabled
)

IF [%~1]==[--example] (
  SET optionalComponentChosen=true
  SET classPath=%examplePath%,%classPath%
  ECHO Invoice Example included - needs to be enabled in application configuration as well
)

IF [%~1]==[--production] (
  SET productionChosen=true
  SET configuration=%PARENTDIR%configuration\production.yml
)

IF [%~1]==[--detached] (
  SET detachProcess=true
)

SHIFT
GOTO Loop
:Continue

REM If no optional component is chosen, enable REST and Webapps.
REM If production mode is not chosen, also enable Swagger UI and the example application.
setlocal enabledelayedexpansion
IF [%optionalComponentChosen%]==[false] (
  SET restChosen=true
  ECHO REST API enabled
  ECHO WebApps enabled
  IF [%productionChosen%]==[false] (
    SET swaggeruiChosen=true
    ECHO Swagger UI enabled
    ECHO Invoice Example included - needs to be enabled in application configuration as well
    SET classPath=%swaggerPath%,%examplePath%,%classPath%
  )
  SET classPath=%webappsPath%,%restPath%,!classPath!
)
setlocal disabledelayedexpansion

REM if Swagger UI is enabled but REST is not, warn the user
IF [%swaggeruiChosen%]==[true] (
  IF [%restChosen%]==[false] (
    ECHO You did not enable the REST API. Swagger UI will not be able to send any requests to this Camunda Platform Run instance.
  )
)

ECHO classpath: %classPath%

REM start the application
IF [%detachProcess%]==[true] (
  REM in the background
  start "%APPNAME%" %JAVA% -Dloader.path="%classPath%" -Dcamunda.DEPLOYMENTDIR=%DEPLOYMENTDIR% %JAVA_OPTS% -jar "%BASEDIR%camunda-bpm-run-core.jar" --spring.config.location=file:"%configuration%"

  REM open a browser
  ping -n 5 localhost > NULL
  start http://localhost:8080/
) ELSE (
  call %JAVA% -Dloader.path="%classPath%" -Dcamunda.DEPLOYMENTDIR=%DEPLOYMENTDIR% %JAVA_OPTS% -jar "%BASEDIR%camunda-bpm-run-core.jar" --spring.config.location=file:"%configuration%"
)

GOTO End

:Stop
REM remove argument
SHIFT

REM shut down Camunda Run
ECHO Camunda Run is shutting down.
TASKKILL /FI "WINDOWTITLE eq %APPNAME%"

GOTO End

:Help
ECHO Usage: run.bat [start^|stop] (options...)

:End