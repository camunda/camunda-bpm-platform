package org.camunda.bpm.client.spring.boot.starter.task;

import org.camunda.bpm.client.spring.SubscribedExternalTaskBean;
import org.camunda.bpm.client.spring.SubscriptionInformation;
import org.camunda.bpm.client.spring.boot.starter.CamundaBpmClientProperties;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

public class PropertiesAwareSubscribedExternalTaskBean extends SubscribedExternalTaskBean implements InitializingBean {

  @Autowired
  private CamundaBpmClientProperties camundaBpmClientProperties;

  @Override
  public void afterPropertiesSet() throws Exception {
    mergeSubscriptionInformationWithProperties();
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
      return merge;
    });
    setSubscriptionInformation(merge);
  }
}
