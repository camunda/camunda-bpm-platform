@echo off

SET webappsPath=..\lib\webapps\camunda-rest-distro-webapps-1.0-SNAPSHOT.jar, 
SET libPath=..\lib\db\*

IF "%1"=="--webapps" (
	SET classPath=%webappsPath%%libPath%
	ECHO starting with webapps
	) else (
	SET classPath=%libPath%
	ECHO starting without webapps
	)
	ECHO %classPath%
start java -Dloader.path="%classPath%" -jar ..\lib\camunda-rest-distro-rest-1.0-SNAPSHOT.jar --spring.config.location=file:..\config\application.yml 