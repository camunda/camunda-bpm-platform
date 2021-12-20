# Camunda Platform Quarkus Extensions

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.camunda.bpm.quarkus/camunda-bpm-quarkus-engine/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.camunda.bpm.quarkus/camunda-bpm-quarkus-engine) [![camunda manual latest](https://img.shields.io/badge/manual-latest-brown.svg)](https://docs.camunda.org/manual/develop/user-guide/quarkus-integration/)

This sub-project provides Camunda Platform Quarkus Extensions that allow you to add behavior to your Quarkus 
application by adding dependencies to the classpath.

You can find the documentation on the Camunda Platform Quarkus Extensions 
[here](https://docs.camunda.org/manual/develop/user-guide/quarkus-integration/).

We also provide some useful examples at our 
[camunda-bpm-examples](https://github.com/camunda/camunda-bpm-examples/tree/master/quarkus-extension) repository.

```xml
<dependency>
  <dependency>
    <groupId>org.camunda.bpm.quarkus</groupId>
    <artifactId>camunda-bpm-quarkus-engine</artifactId>
    <version>${version.camunda}</version><!-- place Camunda version here -->
  </dependency>
</dependency>
```

To configure a Camunda Platform Quarkus extension, you can use an `application.properties` file. It
can look like the following:

```properties
# process engine configuration
quarkus.camunda.cmmn-enabled=false
quarkus.camunda.dmn-enabled=false
quarkus.camunda.history=none
quarkus.camunda.initialize-telemetry=false

# job executor configuration
quarkus.camunda.job-executor.thread-pool.max-pool-size=12
quarkus.camunda.job-executor.thread-pool.queue-size=5
quarkus.camunda.job-executor.max-jobs-per-acquisition=5
quarkus.camunda.job-executor.lock-time-in-millis=500000
quarkus.camunda.job-executor.wait-time-in-millis=7000
quarkus.camunda.job-executor.max-wait=65000
quarkus.camunda.job-executor.backoff-time-in-millis=5

# custom data source configuration and selection
quarkus.datasource.my-datasource.db-kind=h2
quarkus.datasource.my-datasource.username=camunda
quarkus.datasource.my-datasource.password=camunda
quarkus.datasource.my-datasource.jdbc.url=jdbc:h2:mem:camunda;TRACE_LEVEL_FILE=0;DB_CLOSE_ON_EXIT=FALSE
quarkus.camunda.datasource=my-datasource
```