package org.camunda.bpm.client.spring.context;

import java.util.Optional;

import org.camunda.bpm.client.spring.DisableDefaultExternalTaskRegistration;
import org.camunda.bpm.client.spring.EnableTaskSubscription;
import org.camunda.bpm.client.spring.ExternalTaskClientFactory;
import org.camunda.bpm.client.spring.helper.AnnotationNullValueHelper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AutowireCandidateQualifier;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ClientRegistrar implements ImportBeanDefinitionRegistrar {

  public static final Class<? extends ExternalTaskClientFactory> DEFAULT_EXTERNAL_TASK_CLIENT_FACTORY_CLASS = ExternalTaskClientFactory.class;

  private final Class<? extends ExternalTaskClientFactory> externalTaskClientFactoryClass;

  public ClientRegistrar() {
    this(DEFAULT_EXTERNAL_TASK_CLIENT_FACTORY_CLASS);
  }

  public ClientRegistrar(Class<? extends ExternalTaskClientFactory> externalTaskClientFactoryClass) {
    Assert.notNull(externalTaskClientFactoryClass, "externalTaskClientFactoryClass must not be 'null'");
    this.externalTaskClientFactoryClass = externalTaskClientFactoryClass;
  }

  @Override
  public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
    registerExternalTaskClient(importingClassMetadata, registry);
  }

  protected void registerExternalTaskClient(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
    AnnotationAttributes enableTaskSubscription = getEnableTaskSubscription(importingClassMetadata);
    String id = getId(enableTaskSubscription);
    String beanName = "externalTaskClient" + id;
    if (!isUniqueBean(beanName, importingClassMetadata, registry)) {
      return;
    }
    BeanDefinitionBuilder builder = getExternalTaskClientFactoryBeanDefinitionBuilder(enableTaskSubscription, id);
    if (!StringUtils.isEmpty(id)) {
      AutowireCandidateQualifier qualifierMetadata = new AutowireCandidateQualifier(Qualifier.class);
      qualifierMetadata.setAttribute(AutowireCandidateQualifier.VALUE_KEY, id);
      builder.getRawBeanDefinition().addQualifier(qualifierMetadata);
    }
    registry.registerBeanDefinition(beanName, builder.getBeanDefinition());
    log.debug("registered external task client with beanName '{}'", beanName);
  }

  protected BeanDefinitionBuilder getExternalTaskClientFactoryBeanDefinitionBuilder(AnnotationAttributes enableTaskSubscription, String id) {
    // @formatter:off
    return BeanDefinitionBuilder.genericBeanDefinition(externalTaskClientFactoryClass).addPropertyValue("baseUrl", getBaseUrl(enableTaskSubscription))
        .addPropertyValue("id", id).addPropertyValue("maxTasks", getMaxTasks(enableTaskSubscription))
        .addPropertyValue("workerId", getWorkerId(enableTaskSubscription))
        .addPropertyValue("asyncResponseTimeout", getAsyncResponseTimeout(enableTaskSubscription))
        .addPropertyValue("autoFetchingEnabled", getAutoFetchingEnabled(enableTaskSubscription))
        .addPropertyValue("lockDuration", getLockDuration(enableTaskSubscription));
    // @formatter:on
  }

  protected boolean isUniqueBean(String beanName, AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
    if (registry.containsBeanDefinition(beanName)) {
      BeanDefinition beanDefinition = registry.getBeanDefinition(beanName);
      log.debug("skipping creation of bean for factory '{}'. A bean with name '{}' created by '{}' already exists", importingClassMetadata.getClassName(),
          beanName, beanDefinition.getFactoryBeanName());
      return false;
    }
    return true;
  }

  public static String getId(AnnotationAttributes annotationAttributes) {
    return annotationAttributes.getString("id");
  }

  public static String getBaseUrl(AnnotationAttributes annotationAttributes) {
    return annotationAttributes.getString("baseUrl");
  }

  public static Integer getMaxTasks(AnnotationAttributes annotationAttributes) {
    return annotationAttributes.getNumber("maxTasks");
  }

  public static Long getLockDuration(AnnotationAttributes annotationAttributes) {
    return annotationAttributes.getNumber("lockDuration");
  }

  public static boolean getAutoFetchingEnabled(AnnotationAttributes annotationAttributes) {
    return annotationAttributes.getBoolean("autoFetchingEnabled");
  }

  public static Long getAsyncResponseTimeout(AnnotationAttributes annotationAttributes) {
    return AnnotationNullValueHelper.respectNullValue(annotationAttributes.getNumber("asyncResponseTimeout"));
  }

  public static String getWorkerId(AnnotationAttributes annotationAttributes) {
    return AnnotationNullValueHelper.respectNullValue(annotationAttributes.getString("workerId"));
  }

  public static AnnotationAttributes getEnableTaskSubscription(AnnotationMetadata annotationMetadata) {
    return AnnotationAttributes.fromMap(annotationMetadata.getAnnotationAttributes(EnableTaskSubscription.class.getName(), false));
  }

  public static Optional<AnnotationAttributes> getDisableDefaultExternalTaskRegistration(AnnotationMetadata annotationMetadata) {
    return Optional
        .ofNullable(AnnotationAttributes.fromMap(annotationMetadata.getAnnotationAttributes(DisableDefaultExternalTaskRegistration.class.getName(), false)));
  }
}
