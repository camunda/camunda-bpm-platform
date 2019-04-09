[![Build Status](https://travis-ci.org/osteinhauer/camunda-external-task-client-java-spring.svg?branch=master)](https://travis-ci.org/osteinhauer/camunda-external-task-client-java-spring)

# Camunda External Task Client as Spring Boot Starter

This project provides a Spring Boot starter that allows you to implement an external task worker for Camunda. It uses the Camunda REST API to fetch, lock and complete external services tasks. It is based on the [Camunda External Task Client](https://github.com/camunda/camunda-external-task-client-java)

# UNDER DEVELOPMENT

This project is not yet there and still need to be worked on.

There is an implementation here: https://github.com/osteinhauer/camunda-external-task-client-java-spring. Currently undecided if this will be based on that code or not.

# Dependency

You need this dependency in order to get started

```xml
<dependencies>
  ...
  <dependency>
    <groupId>org.camunda.bpm.extension.spring.boot</groupId>
    <artifactId>camunda-external-task-client-spring-boot-starter</artifactId>
    <version>...</version>
  </dependency>
  ...
</dependencies>
```



# External Task Subscription

  ```java
  @ExternalTaskSubscription("invoiceCreator")
  @Component
  public class InvoiceCreator implents ExternalTaskHandler {
      void execute(ExternalTask externalTask, ExternalTaskService externalTaskService);
  }
  ```

# Configuration

In order to enable the external task subscriptions you have to do use the `EnableExternalTaskSubscriptions` annotation. You can configure the Camunda endpoint and other properties there.

```java
@Configuration
@EnableExternalTaskSubscriptions(baseUrl = "http://localhost:8080/rest")
public class SimpleConfiguration {
}
```

You can also use other ways to configure it via normal Spring possibilities:

```yaml
camunda:
  bpm:
    client:
      base-url: http://localhost:8080/rest
```


Check tests and examples for usage.


# Use Spring (not Spring Boot)

You can also use the basic Spriung integration without the Spring Boot Starter:

```xml
<dependencies>
  ...
  <dependency>
    <groupId>org.camunda.bpm.extension.spring</groupId>
    <artifactId>camunda-external-task-client-spring</artifactId>
    <version>...</version>
  </dependency>
  ...
</dependencies>
```


# Todo / Future features

- Connection resilience / retrying in case the engine is not available (also during startup of the client)

- Multiple ExternalTask subsctiptions on one handler?

  ```java  
  @ExternalTask("invoiceCreator")
  @ExternalTask(topicName = "invoiceCreator", lockDuration = 1000)
  @ExternalTask(topicName = "offerCreator", lockDuration = 2000)
  @Component
  public class InvoiceCreator implents ExternalTaskHandler {
      void execute(ExternalTask externalTask, ExternalTaskService externalTaskService);
  }
  ```