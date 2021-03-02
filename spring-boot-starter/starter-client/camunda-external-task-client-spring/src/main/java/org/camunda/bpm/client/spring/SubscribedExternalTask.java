package org.camunda.bpm.client.spring;

import java.util.Collection;

import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.spring.SubscribedExternalTaskBean.Subscription;
import org.camunda.bpm.client.topic.TopicSubscription;

public interface SubscribedExternalTask extends TopicSubscription {

  SubscribedExternalTask subscribe(ExternalTaskClient externalTaskClient);

  boolean isSubscribed(ExternalTaskClient externalTaskClient);

  SubscribedExternalTask open(ExternalTaskClient externalTaskClient);

  boolean isAutoSubscribe();

  boolean isAutoOpen();

  boolean isOpen(ExternalTaskClient externalTaskClient);

  void start();

  void start(ExternalTaskClient externalTaskClients);

  void register(ExternalTaskClient externalTaskClient);

  boolean isRegistered(ExternalTaskClient externalTaskClient);

  Collection<Subscription> getSubscriptions();

  SubscriptionInformation getSubscriptionInformation();

}
