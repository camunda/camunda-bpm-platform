@echo off

SET webappsPath=../lib/camunda-rest-distro-webapps-1.0-SNAPSHOT.jar
SET restPath=../lib/camunda-rest-distro-rest-1.0-SNAPSHOT.jar
SET libPath=../lib/db/

SET classPath=%libPath%
SET startJar=

:Loop
IF [%~1]==[] GOTO Continue

IF [%~1]==[--webapps] (
  IF [%startJar%]==[] (
    REM REST not (yet) enabled, start with WebApps
    SET startJar=%webappsPath%
  ) ELSE (
    REM REST already enabled, put WebApps on classpath
    SET classPath=%webappsPath%,%classPath%
  )
  ECHO WebApps enabled
) 

IF [%~1]==[--rest] (
  IF NOT [%startJar%]==[] (
    REM WebApps already enabled, put them on classpath
    SET classPath=%startJar%,%classPath%
  )
  REM start with REST
  SET startJar=%restPath%
  ECHO REST API enabled
)

SHIFT
GOTO Loop
:Continue

ECHO classpath: %classPath%
ECHO starting JAR: %startJar%

call java -Dloader.path="%classPath%" -jar "%startJar%" --spring.config.location=file:../config/application.yml