#!/bin/bash

if [ ! -z ${1+x} ] && [ $1 = "--webapps" ]; then
   java -Dloader.path="../lib/webapps/camunda-rest-distro-webapps-ee-1.0-SNAPSHOT.jar, ../lib/db/" \
        -jar ../lib/camunda-rest-distro-rest-1.0-SNAPSHOT.jar \
        --spring.config.location=file:../config/application.yml
else
   java -Dloader.path="../lib/db/" \
        -jar ../lib/camunda-rest-distro-rest-1.0-SNAPSHOT.jar \
        --spring.config.location=file:../config/application.yml
fi
