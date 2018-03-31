package org.camunda.bpm.client.spring;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.camunda.bpm.client.ExternalTaskClient;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.BeanFactoryAnnotationUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.context.support.AbstractApplicationContext;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SubscriptionStartingRegistry implements ApplicationContextAware {

  @NonNull
  private final List<ExternalTaskClient> externalTaskClients;
  @NonNull
  private final List<SubscribedExternalTaskBean> subscribedExternalTaskBeans;

  private AbstractApplicationContext applicationContext;

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    this.applicationContext = (AbstractApplicationContext) applicationContext;
  }

  @EventListener
  public void handles(ContextRefreshedEvent event) {
    for (SubscribedExternalTaskBean task : subscribedExternalTaskBeans) {
      for (ExternalTaskClient client : findMatchingClients(task)) {
        task.register(client);
        task.start();
      }
    }
  }

  protected List<ExternalTaskClient> findMatchingClients(SubscribedExternalTask task) {
    String[] clientIds = task.getSubscriptionInformation().getExternalTaskClientIds();
    if (clientIds == null || clientIds.length == 0) {
      return externalTaskClients;
    }
    return Arrays.stream(clientIds).map(this::getExternalTaskClientByQualifier).collect(Collectors.toList());
  }

  protected ExternalTaskClient getExternalTaskClientByQualifier(String qualifier) {
    return BeanFactoryAnnotationUtils.qualifiedBeanOfType(applicationContext.getBeanFactory(), ExternalTaskClient.class,
        qualifier);
  }

}
