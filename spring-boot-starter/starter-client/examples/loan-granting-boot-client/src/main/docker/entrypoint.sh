#!/usr/bin/sh
java -Djava.security.egd=file:/dev/./urandom -Dcamunda.bpm.client.worker-id=$(hostname) -jar /app.jar
