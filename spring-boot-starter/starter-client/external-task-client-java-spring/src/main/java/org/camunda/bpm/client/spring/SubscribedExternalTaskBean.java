package org.camunda.bpm.client.spring;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.spring.helper.ExternalTaskClientHelper;
import org.camunda.bpm.client.task.ExternalTaskHandler;
import org.camunda.bpm.client.topic.TopicSubscription;
import org.camunda.bpm.client.topic.TopicSubscriptionBuilder;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.context.support.AbstractApplicationContext;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

public class SubscribedExternalTaskBean implements SubscribedExternalTask, InitializingBean, ApplicationContextAware {

  private final Map<ExternalTaskClient, Subscription> subscriptions = new HashMap<>();
  @Getter
  @Setter
  private ExternalTaskHandler externalTaskHandler;
  @Getter
  @Setter
  private SubscriptionInformation subscriptionInformation;
  private AbstractApplicationContext applicationContext;

  @Override
  public void afterPropertiesSet() throws Exception {
    ExternalTaskClientHelper.findMatchingClients(applicationContext, this).forEach(this::register);
  }

  @EventListener
  public void start(ApplicationEvent event) {
    if (isEventThatCanStartSubscription().test(event)) {
      start();
    }
  }

  protected Predicate<ApplicationEvent> isEventThatCanStartSubscription() {
    return event -> event instanceof ContextRefreshedEvent;
  }

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
    return subscriptions.values().stream().filter(Objects::nonNull).collect(Collectors.toList());
  }

  @Override
  public SubscribedExternalTask subscribe(ExternalTaskClient externalTaskClient) {
    if (!isSubscribed(externalTaskClient)) {
      Subscription subscription = Subscription.build(this, externalTaskClient);
      subscription.subscribe();
      subscriptions.remove(externalTaskClient);
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
    subscriptions.values().stream().filter(Objects::nonNull).forEach(Subscription::close);
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
  public Long getLockDuration() {
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

  @Override
  public List<String> getVariableNames() {
    return subscriptionInformation.getVariableNames();
  }

  @Override
  public String getBusinessKey() {
    return subscriptionInformation.getBusinessKey();
  }

  @Override
  public String getProcessDefinitionId() {
    return subscriptionInformation.getProcessDefinitionId();
  }

  @Override
  public List<String> getProcessDefinitionIdIn() {
    return subscriptionInformation.getProcessDefinitionIdIn();
  }

  @Override
  public String getProcessDefinitionKey() {
    return subscriptionInformation.getProcessDefinitionKey();
  }

  @Override
  public List<String> getProcessDefinitionKeyIn() {
    return subscriptionInformation.getProcessDefinitionKeyIn();
  }

  @Override
  public boolean isWithoutTenantId() {
    return subscriptionInformation.isWithoutTenantId();
  }

  @Override
  public List<String> getTenantIdIn() {
    return subscriptionInformation.getTenantIdIn();
  }

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    this.applicationContext = (AbstractApplicationContext) applicationContext;
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
        topicSubscriptionBuilder = externalTaskClient.subscribe(subscriptionInformation.getTopicName()).lockDuration(subscriptionInformation.getLockDuration())
            .businessKey(subscriptionInformation.getBusinessKey()).handler(externalTaskHandler);
        List<String> variableNames = subscriptionInformation.getVariableNames();
        if (variableNames != null) {
          topicSubscriptionBuilder.variables(variableNames.toArray(new String[0]));
        }
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
