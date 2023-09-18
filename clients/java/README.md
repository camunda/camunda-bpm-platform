# Camunda External Task Client (Java)


[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.camunda.bpm/camunda-external-task-client/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.camunda.bpm/camunda-external-task-client)

> Are you looking for the Spring Boot External Task Client? This way please: [Spring Boot External Task Client](../../spring-boot-starter/starter-client)

The **Camunda External Task Client (Java)** allows to set up remote Service Tasks for your workflow.

* [Quick Start](https://docs.camunda.org/get-started/quick-start/)
* [Documentation](https://docs.camunda.org/manual/develop/user-guide/ext-client/)
* [Examples](https://github.com/camunda/camunda-bpm-examples/tree/master/clients/java)

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
* Java (supported version by the used Camunda Platform 7)
* Camunda Platform 7

## Maven coordinates
The following Maven coordinate needs to be added to the projects `pom.xml`:
```xml
<dependency>
  <groupId>org.camunda.bpm</groupId>
  <artifactId>camunda-external-task-client</artifactId>
  <version>${version}</version>
</dependency>
```

## Contributing

Have a look at our [contribution guide](https://github.com/camunda/camunda-bpm-platform/blob/master/CONTRIBUTING.md) for how to contribute to this repository.


## License
The source files in this repository are made available under the [Apache License Version 2.0](./LICENSE).
