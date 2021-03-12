package org.camunda.bpm.client.spring.extendedsubscription;

import org.camunda.bpm.client.spring.ExternalTaskSubscription;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskHandler;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.springframework.stereotype.Component;

@ExternalTaskSubscription(topicName = "testClassSubscription", externalTaskClientIds = "externalTaskClientSecond")
@Component
public class ExtendedTestClassSubscription implements ExternalTaskHandler {

  @Override
  public void execute(ExternalTask externalTask, ExternalTaskService externalTaskService) {
    // TODO Auto-generated method stub

  }

}
