package org.camunda.bpm.client.spring.context;

import java.util.Map;
import java.util.Optional;

import org.camunda.bpm.client.spring.SubscribedExternalTaskBean;
import org.camunda.bpm.client.spring.TaskSubscription;
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
import org.springframework.util.Assert;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ExternalTaskBeanDefinitionRegistryPostProcessor implements BeanDefinitionRegistryPostProcessor {

  protected final Class<? extends SubscribedExternalTaskBean> subscribedExternalTaskBeanClass;

  public ExternalTaskBeanDefinitionRegistryPostProcessor() {
    this(SubscribedExternalTaskBean.class);
  }

  public ExternalTaskBeanDefinitionRegistryPostProcessor(Class<? extends SubscribedExternalTaskBean> subscribedExternalTaskBeanClass) {
    Assert.notNull(subscribedExternalTaskBeanClass, "subscribedExternalTaskBeanClass must not be 'null'");
    this.subscribedExternalTaskBeanClass = subscribedExternalTaskBeanClass;
  }

  @Override
  public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
    if (registry instanceof ListableBeanFactory) {
      ListableBeanFactory listableBeanFactory = (ListableBeanFactory) registry;
      String[] beanNamesForType = listableBeanFactory.getBeanNamesForType(ExternalTaskHandler.class);
      log.debug("found beans for {}: {}", ExternalTaskHandler.class, beanNamesForType);
      if (beanNamesForType != null) {
        for (String beanName : beanNamesForType) {
          getSchedulingDefinition(beanName, registry.getBeanDefinition(beanName)).ifPresent(definition -> {
            log.debug("build subscription for {}", beanName);
            BeanDefinitionBuilder builder = getBeanDefinitionBuilderForExternalTaskBean(listableBeanFactory, definition);
            registry.registerBeanDefinition(beanName + "Subscription", builder.getBeanDefinition());
          });
        }
      }
    }
  }

  @Override
  public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
    // nothing to do
  }

  protected BeanDefinitionBuilder getBeanDefinitionBuilderForExternalTaskBean(ListableBeanFactory listableBeanFactory,
      ExternalTaskSubscriptionDefinition definition) {
    BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(subscribedExternalTaskBeanClass)
        .addPropertyReference("externalTaskHandler", definition.getBeanName())
        .addPropertyValue("subscriptionInformation", definition.getSubscriptionInformation()).setDestroyMethodName("close");
    return builder;
  }

  protected Optional<ExternalTaskSubscriptionDefinition> getSchedulingDefinition(String beanName, BeanDefinition beanDefinition) {
    return findSubscriptionAttributes(beanDefinition).map(attributes -> new ExternalTaskSubscriptionDefinition(beanName, attributes));
  }

  protected Optional<Map<String, Object>> findSubscriptionAttributes(BeanDefinition beanDefinition) {
    Map<String, Object> annotationAttributes = null;
    Object source = null;
    if (beanDefinition instanceof AnnotatedBeanDefinition) {
      AnnotatedBeanDefinition annotatedBeanDefinition = (AnnotatedBeanDefinition) beanDefinition;
      source = annotatedBeanDefinition.getFactoryMethodMetadata();
      if (source == null) {
        source = annotatedBeanDefinition.getMetadata();
      }
    }
    if (source == null) {
      source = beanDefinition.getSource();
    }
    if (source instanceof AnnotatedTypeMetadata) {
      AnnotatedTypeMetadata metadata = (AnnotatedTypeMetadata) source;
      annotationAttributes = metadata.getAnnotationAttributes(TaskSubscription.class.getName(), true);
    }
    if (annotationAttributes == null) {
      log.debug("no subscription found for beandefinition {}", beanDefinition);
    } else {
      log.debug("found subscription {} for beandefinition {}", annotationAttributes, beanDefinition);
    }
    return Optional.ofNullable(annotationAttributes);
  }

}
