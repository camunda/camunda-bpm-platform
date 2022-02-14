@echo off

SET BASEDIR=%~dp0
SET deploymentDir=%BASEDIR%configuration/resources

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
SET webappsPath=%BASEDIR%internal\webapps
SET restPath=%BASEDIR%internal\rest
SET swaggerPath=%BASEDIR%internal\swaggerui
SET examplePath=%BASEDIR%internal\example
SET classPath=%BASEDIR%configuration\userlib,%BASEDIR%configuration\keystore
SET optionalComponentChosen=false
SET restChosen=false
SET swaggeruiChosen=false
SET productionChosen=false
SET configuration=%BASEDIR%configuration\default.yml


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
  SET configuration=%BASEDIR%configuration\production.yml
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
call %JAVA% -Dloader.path="%classPath%" -Dcamunda.deploymentDir="%deploymentDir%" %JAVA_OPTS% -jar "%BASEDIR%internal\camunda-bpm-run-core.jar" --spring.config.location=file:"%configuration%"
