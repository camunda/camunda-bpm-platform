/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.client.spring.impl.subscription;

import org.camunda.bpm.client.spring.impl.client.util.ClientLoggerUtil;
import org.camunda.bpm.client.spring.annotation.ExternalTaskSubscription;
import org.camunda.bpm.client.spring.impl.subscription.util.SubscriptionLoggerUtil;
import org.camunda.bpm.client.spring.impl.util.AnnotationUtil;
import org.camunda.bpm.client.task.ExternalTaskHandler;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class SubscriptionPostProcessor implements BeanDefinitionRegistryPostProcessor {

  protected static final SubscriptionLoggerUtil LOG = ClientLoggerUtil.SUBSCRIPTION_LOGGER;

  protected Class<? extends SpringTopicSubscriptionImpl> springTopicSubscription;

  public SubscriptionPostProcessor() {
    this(SpringTopicSubscriptionImpl.class);
  }

  public SubscriptionPostProcessor(Class<? extends SpringTopicSubscriptionImpl> springTopicSubscription) {
    this.springTopicSubscription = springTopicSubscription;
  }

  @Override
  public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
    ListableBeanFactory listableBeanFactory = (ListableBeanFactory) registry;
    String[] handlerBeans = listableBeanFactory.getBeanNamesForType(ExternalTaskHandler.class);
    LOG.handlerBeansFound(ExternalTaskHandler.class, handlerBeans);

    for (String handlerBeanName : handlerBeans) {
      BeanDefinition handlerBeanDefinition = registry.getBeanDefinition(handlerBeanName);
      ExternalTaskSubscription subscriptionAnnotation = findSubscriptionAnnotation(handlerBeanDefinition);
      if (subscriptionAnnotation != null) {
        SubscriptionConfiguration subscriptionConfiguration = new SubscriptionConfiguration();
        subscriptionConfiguration.fromAnnotation(subscriptionAnnotation);
        BeanDefinition subscriptionBeanDefinition = getBeanDefinition(handlerBeanName, subscriptionConfiguration);

        String subscriptionBeanName = handlerBeanName + "Subscription";
        registry.registerBeanDefinition(subscriptionBeanName, subscriptionBeanDefinition);
        LOG.beanRegistered(subscriptionBeanName, handlerBeanName);
      }
    }
  }

  protected BeanDefinition getBeanDefinition(String beanName,
                                             SubscriptionConfiguration subscriptionConfiguration) {
    return BeanDefinitionBuilder.genericBeanDefinition(springTopicSubscription)
        .addPropertyReference("externalTaskHandler", beanName)
        .addPropertyValue("subscriptionConfiguration", subscriptionConfiguration)
        .setDestroyMethodName("closeInternally")
        .getBeanDefinition();
  }

  protected ExternalTaskSubscription findSubscriptionAnnotation(BeanDefinition beanDefinition) {
    ExternalTaskSubscription annotation = null;

    if (beanDefinition instanceof AnnotatedBeanDefinition) {
      AnnotatedBeanDefinition annotatedBeanDefinition = (AnnotatedBeanDefinition) beanDefinition;
      AnnotatedTypeMetadata metadata = annotatedBeanDefinition.getFactoryMethodMetadata();
      if (metadata == null) {
        metadata = annotatedBeanDefinition.getMetadata();
      }
      annotation = AnnotationUtil.get(ExternalTaskSubscription.class, metadata);
    }

    if (annotation == null) {
      LOG.notFound(beanDefinition);
    } else {
      LOG.found(annotation, beanDefinition);
    }
    return annotation;
  }

  @Override
  public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
  }

}
