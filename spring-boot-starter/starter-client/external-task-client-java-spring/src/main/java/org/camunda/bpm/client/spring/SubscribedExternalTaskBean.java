package org.camunda.bpm.client.spring;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.task.ExternalTaskHandler;
import org.camunda.bpm.client.topic.TopicSubscription;
import org.camunda.bpm.client.topic.TopicSubscriptionBuilder;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

public class SubscribedExternalTaskBean implements SubscribedExternalTask {

  private Map<ExternalTaskClient, Subscription> subscriptions = new HashMap<>();
  @Getter
  @Setter
  private ExternalTaskHandler externalTaskHandler;
  @Getter
  @Setter
  private SubscriptionInformation subscriptionInformation;

  @Override
  public void register(ExternalTaskClient externalTaskClient) {
    if (!isRegistered(externalTaskClient)) {
      subscriptions.put(externalTaskClient, Subscription.build(this, externalTaskClient));
    }
  }

  @Override
  public boolean isRegistered(ExternalTaskClient externalTaskClient) {
    return subscriptions.containsKey(externalTaskClient);
  }

  @Override
  public Collection<Subscription> getSubscriptions() {
    return subscriptions.values().stream().filter(s -> s != null).collect(Collectors.toList());
  }

  @Override
  public SubscribedExternalTask subscribe(ExternalTaskClient externalTaskClient) {
    if (!isSubscribed(externalTaskClient)) {
      Subscription subscription = Subscription.build(this, externalTaskClient);
      subscription.subscribe();
      if (subscriptions.containsKey(externalTaskClient)) {
        subscriptions.remove(externalTaskClient);
      }
      subscriptions.put(externalTaskClient, subscription);
    }
    return this;
  }

  @Override
  public boolean isSubscribed(ExternalTaskClient externalTaskClient) {
    return getSubscription(externalTaskClient).map(Subscription::isSubscribed).orElse(false);
  }

  protected Optional<Subscription> getSubscription(ExternalTaskClient externalTaskClient) {
    return Optional.ofNullable(subscriptions.get(externalTaskClient));
  }

  @Override
  public SubscribedExternalTask open(ExternalTaskClient externalTaskClient) {
    getSubscription(externalTaskClient).ifPresent(Subscription::open);
    return this;
  }

  @Override
  public void close() {
    subscriptions.values().stream().filter(s -> s != null).forEach(Subscription::close);
  }

  @Override
  public void start() {
    subscriptions.keySet().forEach(this::start);
  }

  @Override
  public void start(ExternalTaskClient externalTaskClient) {
    getSubscription(externalTaskClient).ifPresent(Subscription::start);
  }

  @Override
  public boolean isOpen(ExternalTaskClient externalTaskClient) {
    return getSubscription(externalTaskClient).map(Subscription::isOpen).orElse(false);
  }

  @Override
  public String getTopicName() {
    return subscriptionInformation.getTopicName();
  }

  @Override
  public long getLockDuration() {
    return subscriptionInformation.getLockDuration();
  }

  @Override
  public boolean isAutoSubscribe() {
    return subscriptionInformation.isAutoSubscribe();
  }

  @Override
  public boolean isAutoOpen() {
    return subscriptionInformation.isAutoOpen();
  }

  @RequiredArgsConstructor
  public static class Subscription {
    private final ExternalTaskHandler externalTaskHandler;
    private final SubscriptionInformation subscriptionInformation;
    private final ExternalTaskClient externalTaskClient;
    private TopicSubscriptionBuilder topicSubscriptionBuilder;
    private TopicSubscription topicSubscription;
    private boolean started;

    public static Subscription build(SubscribedExternalTaskBean bean, ExternalTaskClient taskClient) {
      return new Subscription(bean.getExternalTaskHandler(), bean.getSubscriptionInformation(), taskClient);
    }

    public void subscribe() {
      if (!isSubscribed()) {
        topicSubscriptionBuilder = externalTaskClient.subscribe(subscriptionInformation.getTopicName())
            .lockDuration(subscriptionInformation.getLockDuration()).handler(externalTaskHandler);
      }
    }

    public boolean isSubscribed() {
      return externalTaskClient != null && topicSubscriptionBuilder != null;
    }

    public void open() {
      if (isSubscribed()) {
        topicSubscription = topicSubscriptionBuilder.open();
      }
    }

    public boolean isOpen() {
      return topicSubscription != null;
    }

    public void close() {
      if (topicSubscription != null) {
        topicSubscription.close();
        topicSubscription = null;
        topicSubscriptionBuilder = null;
      }
    }

    public void start() {
      if (!started) {
        if (subscriptionInformation.isAutoSubscribe() || subscriptionInformation.isAutoOpen()) {
          subscribe();
        }
        if (subscriptionInformation.isAutoOpen()) {
          open();
        }
        started = true;
      }
    }
  }

}
