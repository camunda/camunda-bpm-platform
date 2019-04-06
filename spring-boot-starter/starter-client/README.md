[![Build Status](https://travis-ci.org/osteinhauer/camunda-external-task-client-java-spring.svg?branch=master)](https://travis-ci.org/osteinhauer/camunda-external-task-client-java-spring)

# Spring and Spring Boot Support for camunda-external-task-client-java

See https://github.com/camunda/camunda-external-task-client-java

Support for `@EnableTaskSubscription` and `@TaskSubscription`. 

## Example Usage

### Spring

```xml
<dependencies>
  ...
  <dependency>
    <groupId>org.camunda.bpm.spring.boot</groupId>
    <artifactId>external-task-client-java-spring</artifactId>
    <version>${project.version}</version>
  </dependency>
  <dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-context</artifactId>
    <version>${spring.version}</version>
  </dependency>
</dependencies>
```

```java
@Configuration
@EnableTaskSubscription(baseUrl = "http://localhost:8080/rest")
public class SimpleConfiguration {

  @TaskSubscription(topicName = "creditScoreChecker")
  @Bean
  public ExternalTaskHandler creditScoreCheckerHandler() {
    return (externalTask, externalTaskService) -> {

      // interact with the external task

    };
  }
}
```

### Spring Boot

```xml
<dependencies>
  ...
  <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter</artifactId>
  </dependency>
  <dependency>
    <groupId>org.camunda.bpm.spring.boot</groupId>
    <artifactId>external-task-client-java-starter</artifactId>
    <version>${project.version}</version>
  </dependency>
</dependencies>
```

```java
@Configuration
public class SimpleConfiguration {

  @TaskSubscription(topicName = "creditScoreChecker")
  @Bean
  public ExternalTaskHandler creditScoreCheckerHandler() {
    return (externalTask, externalTaskService) -> {

      // interact with the external task

    };
  }
}
```

```yaml
camunda:
  bpm:
    client:
      base-url: http://localhost:8080/rest
```


Check tests and examples for usage.
