package org.camunda.bpm.client.spring;

import org.camunda.bpm.client.spring.extendedsubscription.ExtendedTestClassSubscription;
import org.camunda.bpm.client.task.ExternalTaskHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackageClasses = { ExtendedTestClassSubscription.class })
public class ExtendedConfiguration {

  @Configuration
  @EnableTaskSubscription(baseUrl = "http://localhost:8080/rest", id = "First")
  static class FirstConfig {

    @TaskSubscription(topicName = "methodSubscription", externalTaskClientIds = "externalTaskClientFirst")
    @Bean
    public ExternalTaskHandler methodSubscription() {
      return (externalTask, externalTaskService) -> {

        // interact with the external task

      };
    }
  }

  @Configuration
  @EnableTaskSubscription(baseUrl = "http://localhost:8090/rest", id = "Second")
  static class SecondConfig {

  }
}
