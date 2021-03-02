package org.camunda.bpm.spring.boot.example.loangranting.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.camunda.bpm.client.spring.ExternalTaskSubscription;
import org.camunda.bpm.client.spring.boot.starter.CamundaBpmClientProperties;
import org.camunda.bpm.client.task.ExternalTaskHandler;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.value.ObjectValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TaskHandlerConfiguration {

  private final String workerId;
  
  private static Logger log = LoggerFactory.getLogger(TaskHandlerConfiguration.class);

  public TaskHandlerConfiguration(CamundaBpmClientProperties properties) {
    workerId = properties.getWorkerId();
  }

  @ExternalTaskSubscription(topicName = "creditScoreChecker")
  @Bean
  public ExternalTaskHandler creditScoreChecker() {
    return (externalTask, externalTaskService) -> {

      // retrieve a variable from the Workflow Engine
      int defaultScore = externalTask.getVariable("defaultScore");

      List<Integer> creditScores = new ArrayList<>(Arrays.asList(defaultScore, 9, 1, 4, 10));

      // create an object typed variable
      ObjectValue creditScoresObject = Variables.objectValue(creditScores).create();

      // complete the external task
      externalTaskService.complete(externalTask, Variables.putValueTyped("creditScores", creditScoresObject));

      log.info("{}: The External Task {} has been checked!", workerId, externalTask.getId());
    };
  }

  @ExternalTaskSubscription(topicName = "loanGranter")
  @Bean
  public ExternalTaskHandler loanGranter() {
    return (externalTask, externalTaskService) -> {
      int score = externalTask.getVariable("score");
      externalTaskService.complete(externalTask);

      log.info("{}: The External Task {} has been granted with score {}!", workerId, externalTask.getId(), score);
    };
  }

  @ExternalTaskSubscription(topicName = "requestRejecter")
  @Bean
  public ExternalTaskHandler requestRejecter() {
    return (externalTask, externalTaskService) -> {
      int score = externalTask.getVariable("score");
      externalTaskService.complete(externalTask);

      log.info("{}: The External Task {} has been rejected with score {}!", workerId, externalTask.getId(), score);
    };
  }
}
