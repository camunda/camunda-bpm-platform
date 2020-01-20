@echo off

SET webappsPath=../lib/webapps/
SET restPath=../lib/rest/
SET classPath=../lib/db/

:Loop
IF [%~1]==[] GOTO Continue

IF [%~1]==[--webapps] (
  SET classPath=%webappsPath%,%classPath%
  ECHO WebApps enabled
) 

IF [%~1]==[--rest] (
  SET classPath=%restPath%,%classPath%
  ECHO REST API enabled
)

SHIFT
GOTO Loop
:Continue

ECHO classpath: %classPath%

call java -Dloader.path="%classPath%" -jar "../lib/camunda-bpm-rest-distro-1.0-SNAPSHOT.jar" --spring.config.location=file:../config/application.yml