#!/bin/bash

java -Dloader.path="../lib/webapps/camunda-rest-distro-webapps-1.0-SNAPSHOT.jar" \
     -jar ../lib/camunda-rest-distro-1.0-SNAPSHOT.jar \
     --spring.config.location=file:../config/application.yml