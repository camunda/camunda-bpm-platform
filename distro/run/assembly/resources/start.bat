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


REM set environment parameters
SET webappsPath=%BASEDIR%internal\webapps
SET restPath=%BASEDIR%internal\rest
SET classPath=%BASEDIR%configuration\userlib,%BASEDIR%configuration\keystore
SET optionalComponentChosen=false
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
  SET classPath=%restPath%,%classPath%
  ECHO REST API enabled
)

IF [%~1]==[--production] (
  SET configuration=%BASEDIR%configuration\production.yml
)


SHIFT
GOTO Loop
:Continue

REM if neither REST nor Webapps are explicitly chosen, enable both
IF [%optionalComponentChosen%]==[false] (
  ECHO REST API enabled
  ECHO WebApps enabled
  SET classPath=%webappsPath%,%restPath%,%classPath%
)

ECHO classpath: %classPath%


REM start the application
call %JAVA% -Dloader.path="%classPath%" -Dcamunda.deploymentDir="%deploymentDir%" -jar "%BASEDIR%internal\camunda-bpm-run-core.jar" --spring.config.location=file:"%configuration%"
