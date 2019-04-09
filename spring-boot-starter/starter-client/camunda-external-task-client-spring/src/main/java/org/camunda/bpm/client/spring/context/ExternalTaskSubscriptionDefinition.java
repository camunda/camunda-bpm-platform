package org.camunda.bpm.client.spring.context;

import static org.camunda.bpm.client.spring.helper.AnnotationNullValueHelper.respectNullValue;

import java.util.Map;

import org.camunda.bpm.client.spring.SubscriptionInformation;
import org.springframework.util.Assert;

import lombok.Data;

@Data
public class ExternalTaskSubscriptionDefinition {

  private final String beanName;
  private final SubscriptionInformation subscriptionInformation;

  public ExternalTaskSubscriptionDefinition(String beanName, Map<String, Object> subscriptionAttributes) {
    Assert.notNull(beanName, "beanName must not be 'null'");
    Assert.notNull(subscriptionAttributes, "subscriptionMetaData must not be 'null'");
    this.beanName = beanName;
    this.subscriptionInformation = fromMetaData(subscriptionAttributes);
  }

  private static SubscriptionInformation fromMetaData(Map<String, Object> subscriptionMetaData) {
    SubscriptionInformation subscriptionInformation = new SubscriptionInformation((String) subscriptionMetaData.get("topicName"));
    subscriptionInformation.setLockDuration((long) subscriptionMetaData.get("lockDuration"));
    subscriptionInformation.setAutoSubscribe((boolean) subscriptionMetaData.get("autoSubscribe"));
    subscriptionInformation.setAutoOpen((boolean) subscriptionMetaData.get("autoOpen"));
    subscriptionInformation.setExternalTaskClientIds((String[]) subscriptionMetaData.get("externalTaskClientIds"));
    subscriptionInformation.setVariableNames(respectNullValue((String[]) subscriptionMetaData.get("variableNames")));
    subscriptionInformation.setBusinessKey(respectNullValue((String) subscriptionMetaData.get("businessKey")));
    return subscriptionInformation;
  }

}
