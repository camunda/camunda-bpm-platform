# Camunda External Task Client (Java)


[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.camunda.bpm/camunda-external-task-client/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.camunda.bpm/camunda-external-task-client)


The **Camunda External Task Client (Java)** allows to set up remote Service Tasks for your workflow.

* [Quick Start](https://docs.camunda.org/get-started/quick-start/)
* [Documentation](https://docs.camunda.org/manual/develop/user-guide/ext-client/)
* [Examples](https://github.com/camunda/camunda-external-task-client-java/tree/master/examples)

## Features
* Complete External Tasks
* Extend the lock duration of External Tasks
* Unlock External Tasks
* Report BPMN errors as well as failures
* Share primitive and object typed process variables with the Workflow Engine

## Configuration options
* The client can be configured with the fluent api of the [ExternalTaskClientBuilder](client/src/main/java/org/camunda/bpm/client/ExternalTaskClientBuilder.java). 
* The topic subscription can be configured with the fluent api of the [TopicSubscriptionBuilder](client/src/main/java/org/camunda/bpm/client/topic/TopicSubscriptionBuilder.java).

## Prerequisites
* Oracle Hotspot v1.8+ (JDK 8)
* Camunda BPM Platform 7.9.0+

## Maven coordinates
The following Maven coordinate needs to be added to the projects `pom.xml`:
```xml
<dependency>
  <groupId>org.camunda.bpm</groupId>
  <artifactId>camunda-external-task-client</artifactId>
  <version>${version}</version>
</dependency>
```

## License
Unless otherwise specified this project is licensed under [Apache License Version 2.0](./LICENSE).
