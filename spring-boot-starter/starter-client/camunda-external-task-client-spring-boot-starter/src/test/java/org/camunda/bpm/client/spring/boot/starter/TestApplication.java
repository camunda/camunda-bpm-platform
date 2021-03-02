package org.camunda.bpm.client.spring.boot.starter;

import org.camunda.bpm.client.spring.ExternalTaskSubscription;
import org.camunda.bpm.client.task.ExternalTaskHandler;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class TestApplication {

  @ExternalTaskSubscription(topicName = "methodSubscription", autoOpen = false)
  @Bean
  public ExternalTaskHandler methodSubscription() {
    return (externalTask, externalTaskService) -> {

      // interact with the external task

    };
  }

}
