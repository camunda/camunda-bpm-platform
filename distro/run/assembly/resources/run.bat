@echo off

REM set constants
SET BASEDIR=%~dp0
SET PARENTDIR=%BASEDIR%..\
SET DEPLOYMENTDIR=%PARENTDIR%configuration/resources
SET WEBAPPS_PATH=%BASEDIR%webapps
SET REST_PATH=%BASEDIR%rest
SET SWAGGER_PATH=%BASEDIR%swaggerui
SET EXAMPLE_PATH=%BASEDIR%example
SET APPNAME=Camunda Run

IF [%~1]==[start] GOTO Startup
IF [%~1]==[stop] GOTO Stop
IF [%~1]==[help] GOTO Help
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
SET optionalComponentChosen=false
SET restChosen=false
SET swaggeruiChosen=false
SET productionChosen=false
SET detachProcess=false
SET classPath=%PARENTDIR%configuration\userlib,%PARENTDIR%configuration\keystore
SET configuration=%PARENTDIR%configuration\default.yml


REM inspect arguments
:Loop
IF [%~1]==[] GOTO Continue

IF [%~1]==[--webapps] (
  SET optionalComponentChosen=true
  SET classPath=%WEBAPPS_PATH%,%classPath%
  ECHO WebApps enabled
)

IF [%~1]==[--rest] (
  SET optionalComponentChosen=true
  SET restChosen=true
  SET classPath=%REST_PATH%,%classPath%
  ECHO REST API enabled
)

IF [%~1]==[--swaggerui] (
  SET optionalComponentChosen=true
  SET swaggeruiChosen=true
  SET classPath=%SWAGGER_PATH%,%classPath%
  ECHO Swagger UI enabled
)

IF [%~1]==[--example] (
  SET optionalComponentChosen=true
  SET classPath=%EXAMPLE_PATH%,%classPath%
  ECHO Invoice Example included - needs to be enabled in application configuration as well
)

IF [%~1]==[--production] (
  SET productionChosen=true
  SET configuration=%PARENTDIR%configuration\production.yml
)

IF [%~1]==[--detached] (
  SET detachProcess=true
)

IF [%~1]==[--help] (
  GOTO ArgsHelp
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
    SET classPath=%SWAGGER_PATH%,%EXAMPLE_PATH%,%classPath%
  )
  SET classPath=%WEBAPPS_PATH%,%REST_PATH%,!classPath!
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
  start "%APPNAME%" %JAVA% -Dloader.path="%classPath%" -Dcamunda.deploymentDir=%DEPLOYMENTDIR% %JAVA_OPTS% -jar "%BASEDIR%camunda-bpm-run-core.jar" --spring.config.location=file:"%configuration%"

) ELSE (
  call %JAVA% -Dloader.path="%classPath%" -Dcamunda.deploymentDir=%DEPLOYMENTDIR% %JAVA_OPTS% -jar "%BASEDIR%camunda-bpm-run-core.jar" --spring.config.location=file:"%configuration%"
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
:ArgsHelp
ECHO Options:
ECHO   --webapps    - Enables the Camunda Platform Webapps
ECHO   --rest       - Enables the REST API
ECHO   --swaggerui  - Enables the Swagger UI
ECHO   --example    - Enables the example application
ECHO   --production - Applies the production.yaml configuration file
ECHO   --detached   - Starts Camunda Run as a detached process

:End