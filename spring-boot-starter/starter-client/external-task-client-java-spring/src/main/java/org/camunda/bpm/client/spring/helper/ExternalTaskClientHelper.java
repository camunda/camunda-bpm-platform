package org.camunda.bpm.client.spring.helper;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.spring.SubscribedExternalTask;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.BeanFactoryAnnotationUtils;
import org.springframework.context.support.AbstractApplicationContext;

public class ExternalTaskClientHelper {

  public static Collection<ExternalTaskClient> findMatchingClients(AbstractApplicationContext applicationContext, SubscribedExternalTask task) {
    String[] clientIds = task.getSubscriptionInformation().getExternalTaskClientIds();
    if (clientIds == null || clientIds.length == 0) {
      return applicationContext.getBeansOfType(ExternalTaskClient.class).values();
    }
    return Arrays.stream(clientIds).map(id -> getExternalTaskClientByQualifier(applicationContext.getBeanFactory(), id)).collect(Collectors.toList());
  }

  public static ExternalTaskClient getExternalTaskClientByQualifier(BeanFactory beanFactory, String qualifier) {
    return BeanFactoryAnnotationUtils.qualifiedBeanOfType(beanFactory, ExternalTaskClient.class, qualifier);
  }

}
