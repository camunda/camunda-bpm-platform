# Camunda External Task Client as Spring Boot Starter

This project provides a Spring Boot starter that allows you to implement an external task worker for Camunda. It uses the Camunda REST API to fetch, lock and complete external services tasks. 
It is based on the [Camunda External Task Client](https://github.com/camunda/camunda-bpm-platform/clients/java)

## Dependency

You need this dependency to get started:

```xml
<dependencies>
  ...
  <dependency>
    <groupId>org.camunda.bpm.springboot</groupId>
    <artifactId>camunda-bpm-spring-boot-starter-external-task-client</artifactId>
    <version>...</version>
  </dependency>
  ...
</dependencies>
```

## Configuration

You can configure the Camunda endpoint and other properties in the `application.yml` file:

```yaml
camunda:
  bpm:
    client:
      base-url: http://localhost:8080/engine-rest
```

## External Task Subscription

  ```java
  @ExternalTaskSubscription("invoiceCreator")
  @Component
  public class InvoiceCreator implements ExternalTaskHandler {
      void execute(ExternalTask externalTask, ExternalTaskService externalTaskService);
  }
  ```

## Use Spring (not Spring Boot)

You can also use the basic Spring integration without the Spring Boot Starter:

```xml
<dependencies>
  ...
  <dependency>
    <groupId>org.camunda.bpm</groupId>
    <artifactId>camunda-external-task-client-spring</artifactId>
    <version>...</version>
  </dependency>
  ...
</dependencies>
```

### Configuration

In order to enable the external task subscriptions you have to use the `EnableExternalTaskClient` annotation. 
You can configure the Camunda endpoint and other properties there.

```java
@Configuration
@EnableExternalTaskClient(baseUrl = "http://localhost:8080/engine-rest")
public class SimpleConfiguration {
}
```
