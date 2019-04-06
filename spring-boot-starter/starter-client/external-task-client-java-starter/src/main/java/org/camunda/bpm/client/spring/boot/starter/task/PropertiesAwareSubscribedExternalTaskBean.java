package org.camunda.bpm.client.spring.boot.starter.task;

import java.util.function.Predicate;

import org.camunda.bpm.client.spring.SubscribedExternalTaskBean;
import org.camunda.bpm.client.spring.SubscriptionInformation;
import org.camunda.bpm.client.spring.boot.starter.CamundaBpmClientProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationEvent;

public class PropertiesAwareSubscribedExternalTaskBean extends SubscribedExternalTaskBean {

  @Autowired
  private CamundaBpmClientProperties camundaBpmClientProperties;

  @Override
  public void afterPropertiesSet() throws Exception {
    mergeSubscriptionInformationWithProperties();
    super.afterPropertiesSet();
  }

  @Override
  protected Predicate<ApplicationEvent> isEventThatCanStartSubscription() {
    return event -> event instanceof ApplicationStartedEvent;
  }

  protected void mergeSubscriptionInformationWithProperties() {
    SubscriptionInformation merge = getSubscriptionInformation();
    camundaBpmClientProperties.subscriptionInformationFor(merge.getTopicName()).map(properties -> {
      if (properties.getLockDuration() != null) {
        merge.setLockDuration(properties.getLockDuration());
      }
      if (properties.getAutoOpen() != null) {
        merge.setAutoOpen(properties.getAutoOpen());
      }
      if (properties.getAutoSubscribe() != null) {
        merge.setAutoSubscribe(properties.getAutoSubscribe());
      }
      if (properties.getVariableNames() != null) {
        merge.setVariableNames(properties.getVariableNames());
      }
      if (properties.getBusinessKey() != null) {
        merge.setBusinessKey(properties.getBusinessKey());
      }
      return merge;
    });
    setSubscriptionInformation(merge);
  }
}
