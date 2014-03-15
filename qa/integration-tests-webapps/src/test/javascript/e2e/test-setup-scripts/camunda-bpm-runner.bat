@ECHO ON

call mvn -f "C:\dev\git\camunda-bpm-platform-ee\webapps\camunda-webapp\plugins\pom.xml" clean jetty:run -Pdevelop

PAUSE