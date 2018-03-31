package org.camunda.bpm.client.spring.boot.starter.subscription;

import org.camunda.bpm.client.spring.TaskSubscription;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskHandler;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.springframework.stereotype.Component;

@TaskSubscription(topicName = "test", autoOpen = false)
@Component
public class TestClassSubscription implements ExternalTaskHandler {

  @Override
  public void execute(ExternalTask externalTask, ExternalTaskService externalTaskService) {
    // TODO Auto-generated method stub

  }

}
